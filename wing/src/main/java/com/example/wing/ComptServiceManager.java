package com.example.wing;

import com.example.wing.internal.ComponentServiceLoader;

import java.util.HashMap;
import java.util.Iterator;

/**
 * 組件服務管理類
 */
public class ComptServiceManager {

        private HashMap<Class<? extends IComponentService>, Class<? extends IComponentService>> mRegisterMap;

        private ComptServiceManager() {
            this.mRegisterMap = new HashMap();
        }

        public static ComptServiceManager getInstance() {
            return ComptServiceManager.SingletonHolder.sInstance;
        }

        public void init(){
                Iterator<IComponentService> it = new ComponentServiceLoader<>(IComponentService.class);
                try {
                    while (it.hasNext()) {
                        IComponentService serviceImpl = it.next();
//                        Log.e("IComponentService Impl:", serviceImpl.getClass().getName());
                        Class anInterface =  serviceImpl.getClass().getInterfaces()[0];
//                        Log.e("IComponentServiceinter:",anInterface.getName());
                        ComptServiceManager.getInstance().registerService((Class<? extends IComponentService>) Class.forName(anInterface.getName()+""),(Class<? extends IComponentService>)Class.forName(serviceImpl.getClass().getName()));
                    }
                }catch (Exception exception){

                }
        }

        public void registerService(Class<? extends IComponentService> serviceKey, Class<? extends IComponentService> serviceImpl) {
            if (serviceKey != null && serviceImpl != null) {
                this.mRegisterMap.put(serviceKey, serviceImpl);
            }
        }

        public void unregisterService(Class<? extends IComponentService> serviceKey) {
            this.mRegisterMap.remove(serviceKey);
        }

        private <T extends IComponentService> T getRegisteredService(Class<T> serviceKey) {
            Class clazz = (Class)this.mRegisterMap.get(serviceKey);
            if (clazz == null) {
                return null;
            } else {
                try {
                    return (T)clazz.newInstance();
                } catch (IllegalAccessException var4) {
                    var4.printStackTrace();
                } catch (InstantiationException var5) {
                    var5.printStackTrace();
                }
                return null;
            }
        }

        public <T extends IComponentService> T getComponentService(Class<T> clazz) {
            if (clazz == null) {
                return null;
            }
            return this.getRegisteredService(clazz);
            }

        private static class SingletonHolder {
            private static ComptServiceManager sInstance = new ComptServiceManager();

            private SingletonHolder() {
            }
        }
    }
