# vanillax-framework
NEVER STOP SERVER!!!!!!

Groovy 기반의 기업용 서버 Application(Web, Batch) Framework입니다.
업무 로직수정시 동적 로딩이 되어 운영환경에서도 서버를 절대 절대 절대 재구동할 필요가 없습니다.

## 주요특징
* 완전한 POJO 환경
* Groovy를 이용한 업무로직 및 SQL 개발
* Groovy class들의 완전한 동적로딩. 서버 재구동이 불필요함
* vanillax-batch를 이용하여 주기적 작업을 수행. (Cron Expression을 이용및 주기적 실행 및 특정시간 간격의 작업 모두 가능 )
* vanillax-webmvc를 이용하여 간편한 웹서비스 개발.
* Java개발자에게 직관적인 Web Service 개발환경. (Spring Framework와 유사한 업무로직 클래스 및 Java Persistent 제어 방식 채용)

## 프로젝트 설명
* vanillax-core
   - JDBC : JDBC Connection 및 기바 persistent 제어를 위한 핵심 클래스
   - Object : Groovy Object를 동적으로 로딩하는 핵심 클래스. 서버 재구동없이 Groovy 클래스를 재로딩하여 완전한 동적 로딩을 구현함.
* vanillax-webmvc : Web Application 개발 환경. Rest 서비스 개발에 최적회 되어 있음.
* vanillax-batch : 위 vanillax-webmvc기반위에 개발된 배치 시스템.
   - cron expression 지원
   - 일정 시간간격별 실행 기능 지원

## 예제
* 웹서비스 예제 : [vanillax-framework-webmvc-example](https://github.com/vanillabrain/vanillax-framework-webmvc-example) 예제 참조
* 배치 시스템 예제 : [vanillax-framework-batch-example](https://github.com/vanillabrain/vanillax-framework-batch-example) 참조
