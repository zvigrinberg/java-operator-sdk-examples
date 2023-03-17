# java-operator-sdk-examples
Getting started and developing examples and demos on how to build operators using Java operator SDK..

## Documentation For Creating new operator 
[Scaffold new java operator SDK Application Skeleton](https://github.com/operator-framework/java-operator-plugins/blob/main/docs/tutorial.md) \
Note: You need the Operator Sdk CLI Tool, Download it [here]()  

After The Initial Scaffolding, You can choose Whether to use Quarkus Application or just plain JAVA Application In order to Create an entry point ( main function) for the operator application. 

JAVA Without Quarkus
```java
public class Runner {

    public static void main(String[] args) {
        Operator operator = new Operator();
        operator.register(new WebPageReconciler());
        operator.start();
    }
}
```

QUARKUS
```java
@QuarkusMain
public class QuarkusOperator implements QuarkusApplication {

    @Inject
    Operator operator;

    public static void main(String... args) {
        Quarkus.run(QuarkusOperator.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        operator.start();
        Quarkus.waitForExit();
        return 0;
    }
}
```

SpringBoot:
```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

You will also need a `@Configuration` to make sure that your reconciler is registered:

```java

@Configuration
public class Config {

    @Bean
    public WebPageReconciler customServiceController() {
        return new WebPageReconciler();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @SuppressWarnings("rawtypes")
    public Operator operator(List<Reconciler> controllers) {
        Operator operator = new Operator();
        controllers.forEach(operator::register);
        return operator;
    }
}
```


[Here](https://github.com/java-operator-sdk/java-operator-sdk/blob/main/docs/documentation/use-samples.md) You will find all the details. 
