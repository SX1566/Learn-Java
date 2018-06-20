# GFTaxi traffic-accident Build

交通事故模块构建器。

## Requirement

- Maven 3.5.2+
- Kotlin 1.2.31+
- Java 8+
- Spring Framework 5+
- Spring Boot 2+
- Reactor 3+

## Maven Modules

Name                          | Parent                        | Remark
------------------------------|-------------------------------|--------
[gftaxi-traffic-accident-build]        | [simter-build:0.5.0] |
[gftaxi-traffic-accident-dependencies] | gftaxi-traffic-accident-build          | 依赖管理
[gftaxi-traffic-accident-parent]       | gftaxi-traffic-accident-dependencies   | 各子模块的父模块
[gftaxi-traffic-accident-data]         | gftaxi-traffic-accident-parent         | PO、Service、Dao 接口
[gftaxi-traffic-accident-data-jpa]     | gftaxi-traffic-accident-parent         | Dao 接口的 JPA 实现
[gftaxi-traffic-accident-rest-webflux] | gftaxi-traffic-accidentparent          | Rest 接口
[gftaxi-traffic-accident-starter]      | gftaxi-traffic-accident-parent         | 微服务启动器


[simter-build:0.5.0]: https://github.com/simter/simter-build/tree/0.5.0
[gftaxi-traffic-accident-build]: https://gitee.com/gftaxi/gftaxi-traffic-accident
[gftaxi-traffic-accident-dependencies]: https://gitee.com/gftaxi/gftaxi-traffic-accident/tree/master/gftaxi-traffic-accident-dependencies
[gftaxi-traffic-accident-parent]: https://gitee.com/gftaxi/gftaxi-traffic-accident/tree/master/gftaxi-traffic-accident-parent
[gftaxi-traffic-accident-data]: https://gitee.com/gftaxi/gftaxi-traffic-accident/tree/master/gftaxi-traffic-accident-data
[gftaxi-traffic-accident-data-crawler]: https://gitee.com/gftaxi/gftaxi-traffic-accident/tree/master/gftaxi-traffic-accident-data-crawler
[gftaxi-traffic-accident-data-jpa]: https://gitee.com/gftaxi/gftaxi-traffic-accident/tree/master/gftaxi-traffic-accident-data-jpa
[gftaxi-traffic-accident-rest-webflux]: https://gitee.com/gftaxi/gftaxi-traffic-accident/tree/master/gftaxi-traffic-accident-rest-webflux
[gftaxi-traffic-accident-starter]: https://gitee.com/gftaxi/gftaxi-traffic-accident/tree/master/gftaxi-traffic-accident-starter