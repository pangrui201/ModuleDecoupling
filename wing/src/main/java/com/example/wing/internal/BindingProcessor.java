package com.example.wing.internal;


import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;
import static com.google.auto.common.MoreElements.getAnnotationMirror;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.example.wing.ComponentService;
import com.google.auto.common.MoreTypes;
import com.google.auto.service.AutoService;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@AutoService(Processor.class)
public class BindingProcessor extends AbstractProcessor {


    static final String MISSING_SERVICES_ERROR = "No service interfaces provided for element!";

    /**
     * Maps the class names of service provider interfaces to the
     * class names of the concrete classes which implement them.
     * <p>
     * For example,
     *   {@code "com.google.apphosting.LocalRpcService" ->
     *   "com.google.apphosting.datastore.LocalDatastoreService"}
     */
    private Multimap<String, String> providers = HashMultimap.create();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();  // ??????
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ComponentService.class.getCanonicalName());      // ????????????
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            try {
                log("BindingProcessor process");
                return processImpl(annotations, roundEnv);
            } catch (Exception e) {
                // We don't allow exceptions of any kind to propagate to the compiler
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                fatalError(writer.toString());
                return true;
            }
        }

        private boolean processImpl (Set < ? extends TypeElement > annotations, RoundEnvironment
        roundEnv){
            if (roundEnv.processingOver()) {
                generateConfigFiles();
            } else {
                processAnnotations(annotations, roundEnv);
            }

            return true;
        }

        private void processAnnotations (Set < ? extends TypeElement > annotations,
                RoundEnvironment roundEnv){

            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ComponentService.class);

            log(annotations.toString());
            log(elements.toString());

            for (Element e : elements) {
                // TODO(gak): check for error trees?
                TypeElement providerImplementer = (TypeElement) e;
                AnnotationMirror annotationMirror = getAnnotationMirror(e, ComponentService.class).get();
                Set<DeclaredType> providerInterfaces = getValueFieldOfClasses(annotationMirror);
                if (providerInterfaces.isEmpty()) {
                    error(MISSING_SERVICES_ERROR, e, annotationMirror);
                    continue;
                }
                for (DeclaredType providerInterface : providerInterfaces) {
                    TypeElement providerType = MoreTypes.asTypeElement(providerInterface);

                    log("provider interface: " + providerType.getQualifiedName());
                    log("provider implementer: " + providerImplementer.getQualifiedName());

                    if (checkImplementer(providerImplementer, providerType)) {
                        providers.put(getBinaryName(providerType), getBinaryName(providerImplementer));
                    } else {
                        String message = "ServiceProviders must implement their service provider interface. "
                                + providerImplementer.getQualifiedName() + " does not implement "
                                + providerType.getQualifiedName();
                        error(message, e, annotationMirror);
                    }
                }
            }
        }

        private void generateConfigFiles () {
            Filer filer = processingEnv.getFiler();

            for (String providerInterface : providers.keySet()) {
                String resourceFile = "META-INF/services/" + providerInterface;
                log("Working on resource file: " + resourceFile);
                try {
                    SortedSet<String> allServices = Sets.newTreeSet();
                    try {
                        // would like to be able to print the full path
                        // before we attempt to get the resource in case the behavior
                        // of filer.getResource does change to match the spec, but there's
                        // no good way to resolve CLASS_OUTPUT without first getting a resource.
                        FileObject existingFile = filer.getResource(StandardLocation.CLASS_OUTPUT, "",
                                resourceFile);
                        log("Looking for existing resource file at " + existingFile.toUri());
                        Set<String> oldServices = ServicesFiles.readServiceFile(existingFile.openInputStream());
                        log("Existing service entries: " + oldServices);
                        allServices.addAll(oldServices);
                    } catch (IOException e) {
                        // According to the javadoc, Filer.getResource throws an exception
                        // if the file doesn't already exist.  In practice this doesn't
                        // appear to be the case.  Filer.getResource will happily return a
                        // FileObject that refers to a non-existent file but will throw
                        // IOException if you try to open an input stream for it.
                        log("Resource file did not already exist.");
                    }

                    Set<String> newServices = new HashSet<String>(providers.get(providerInterface));
                    if (allServices.containsAll(newServices)) {
                        log("No new service entries being added.");
                        return;
                    }

                    allServices.addAll(newServices);
                    log("New service file contents: " + allServices);
                    FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "",
                            resourceFile);
                    OutputStream out = fileObject.openOutputStream();
                    ServicesFiles.writeServiceFile(allServices, out);
                    out.close();
                    log("Wrote to: " + fileObject.toUri());
                } catch (IOException e) {
                    fatalError("Unable to create " + resourceFile + ", " + e);
                    return;
                }
            }
        }

        /**
         * Verifies {@link } constraints on the concrete provider class.
         * Note that these constraints are enforced at runtime via the ServiceLoader,
         * we're just checking them at compile time to be extra nice to our users.
         */
        private boolean checkImplementer (TypeElement providerImplementer, TypeElement providerType)
        {

            String verify = processingEnv.getOptions().get("verify");
            if (verify == null || !Boolean.valueOf(verify)) {
                return true;
            }

            // TODO: We're currently only enforcing the subtype relationship
            // constraint. It would be nice to enforce them all.

            Types types = processingEnv.getTypeUtils();

            return types.isSubtype(providerImplementer.asType(), providerType.asType());
        }

        /**
         * Returns the binary name of a reference type. For example,
         * {@code com.google.Foo$Bar}, instead of {@code com.google.Foo.Bar}.
         *
         */
        private String getBinaryName (TypeElement element){
            return getBinaryNameImpl(element, element.getSimpleName().toString());
        }

        private String getBinaryNameImpl (TypeElement element, String className){
            Element enclosingElement = element.getEnclosingElement();

            if (enclosingElement instanceof PackageElement) {
                PackageElement pkg = (PackageElement) enclosingElement;
                if (pkg.isUnnamed()) {
                    return className;
                }
                return pkg.getQualifiedName() + "." + className;
            }

            TypeElement typeElement = (TypeElement) enclosingElement;
            return getBinaryNameImpl(typeElement, typeElement.getSimpleName() + "$" + className);
        }

        /**
         * Returns the contents of a {@code Class[]}-typed "value" field in a given {@code annotationMirror}.
         */
        private ImmutableSet<DeclaredType> getValueFieldOfClasses (AnnotationMirror annotationMirror)
        {
            return getAnnotationValue(annotationMirror, "value")
                    .accept(
                            new SimpleAnnotationValueVisitor8<ImmutableSet<DeclaredType>, Void>() {
                                @Override
                                public ImmutableSet<DeclaredType> visitType(TypeMirror typeMirror, Void v) {
                                    // TODO(ronshapiro): class literals may not always be declared types, i.e. int.class,
                                    // int[].class
                                    return ImmutableSet.of(MoreTypes.asDeclared(typeMirror));
                                }

                                @Override
                                public ImmutableSet<DeclaredType> visitArray(
                                        List<? extends AnnotationValue> values, Void v) {
                                    return values
                                            .stream()
                                            .flatMap(value -> value.accept(this, null).stream())
                                            .collect(toImmutableSet());
                                }
                            },
                            null);
        }

        private void log (String msg){
            if (processingEnv.getOptions().containsKey("debug")) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
            }
        }

        private void error (String msg, Element element, AnnotationMirror annotation){
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element, annotation);
        }

        private void fatalError (String msg){
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg);
        }
    }