# ModuleDecoupling
## 背景
随着Android APP业务需求的不断迭代，业务功能越来越多，代码量也越来越多，组件间难免会因为方法功能调用页面访问等场景存在相互访问依赖耦合。为此，探索一种可行方案彻底解决组件模块间耦合同时还能满足组件间相互通信访问显得十分必要。经过不断努力探索调研结合过去工作经验项目总结推出本方案。

## 本方案技术原理如下:
1.组件分两个部分，Module级的接口和对应接口实现Module，达到接口和实现类Module级别的隔离;

2.组件间依赖不在直接依赖实现类Module，而只需依赖其Module接口;

3.通过编译时注解标注具体接口实现类，编译时注解处理器Processor来写实现类class路径文件;

4.app启动运行时通过SPI技术ServiceLoader根据步骤3生成的配置文件将对应的接口和实现类Class作为key,value值注册进公共组件管理模块CmptServiceManager中，而该过程中公共组件是不需要依赖任何组件;

5.当组件a需要使用组件b提供能力时，只需通过公共组件管理类CmptServiceManager.getInstance().getComponmentService(IComponmentBService.class)方法就可以从组件管理模块中拿到组件b的实现类，从访问其接口实现;

6.同时通过定义的Module级接口传递参数，完成组件间通信;

## 本方案有以下特点:
1.解耦彻底，通过编译时注解和Java SPI技术向公共组件管理类中注册组件接口和组件实现类之间对应关系，在注册组件和使用组件时均不需要有业务组件依赖;

2.组件间通信使用简单性能高效，支持所有基本类型和类类型，像在调用Module内部接口一样

3.在main，NebulaIntegration等module多个APP各自维护，逻辑分散，维护成本增大。面向Module级接口编程可以使用一套代码，主逻辑基于接口开发，各自实现差异化，结合可配置方案配置实现类module是否参与编译，实现各自打包。

4.面向接口编程，实现类module升级变化，不会影响使用方，特别适合业务快速迭代场景

5.面向服务编程，每一个Module模块提供的是一种服务能力，通过接口对外暴露服务能力，能有效避免重复开发

6.相比市场上其他方案更加轻量级，没有占用更多cpu内存资源开销



## 系统交互图
![image](https://raw.githubusercontent.com/pangrui201/ModuleDecoupling/main/img/b-%E7%AC%AC%204%20%E9%A1%B5.drawio.png)
![image](https://github.com/ButBueatiful/dotvim/raw/master/screenshots/vim-screenshot.jpg)

![image](https://github.com/pangrui201/ModuleDecoupling/blob/main/img/b-%E6%94%B9%E9%80%A0%E5%89%8D%E7%B3%BB%E7%BB%9F%E4%BA%A4%E4%BA%92%E5%9B%BE-%E7%AC%AC%209%20%E9%A1%B5.drawio.png)
                  图1(改造前组件交互图)                                                      图2(改造后组件交互图)

其中，图1为改造前组件交互图，可以看出组件间存在强依赖关系，而图2是组件模块间解耦及通信轻量级实现方案改造后的交互图，在该图中组件包含组件接口和组件实现两个子组件模块，同时接口和实现对应关系会注册到组件服务管理模块。当组件A需要使用组件B提供的服务时直接访问组件服务管理从而获得B服务提供的能力，而不用依赖B服务。

## **业务时序图**  ![tup](img\时序图.drawio.png)

通过时序图可以看出，在app启动后服务加载器ServiceLoader会统一加载系统中组件服务class文件，注册到组件服务管理中统一管理。当组件A需要使用组件B中的服务时直接从组件管理获取B组件实例即可，不需要依赖组件B。同理，组件B使用组件C服务也是如此。

## **内部子系统说明**

### 编译时：

app在构建编译时，编译流程图如下：

![tup](img\b-第 4 页.drawio.png)

### 运行时：

注:运行时为组件服务加载注册使用过程，如上业务时序图，这里不再额外补充。

**UML:**
![tup](img\b-组件继承关系图.drawio.png)

组件接口继承关系图

 ![tup](img\b-组件服务管理类.drawio.png)

   组件服务管理类图  



### 相关技术点：

1.编译时注解@ComponentService

2.注解解析器BingProcessor

3.Java SPI技术ServiceLoader

4.Java反射技术

5.Module级接口

6.android组件化开发

