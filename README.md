# vanillax-framework
NEVER NEVER NEVER STOP SERVER!!!!!!

This is Groovy based enterprise Server Application Framework.
You DON'T NEED to STOP SEVER for deployment of classes!!!!!!

Groovy 기반의 기업용 서버 Application(Web, Batch) Framework입니다.
업무 로직 수정시 동적으로 클래스가 로딩되어 운영환경에서도 서버를 절대 절대 절대 재구동할 필요가 없습니다.

## Features
* Totally POJO Environment.
* Java Developer friendly development environment. (Groovy and Java code all are available)
* Simple Data Structure with Groovy Object. You don't need define any Value Object, DAO Implementation class etc.
* Fully Dynamic loading of Groovy classes. DON'T STOP SEVER. NEVER! NEVER! NEVER!
* Perfect DB Access resouces monitoring. Supporting JDBC Connection leaking monitoring.
* Spring Framework developer friendly service development environment. (Similar to Spring Framework for Java Persistent control and DI)
    - @Autowired : DI
    - @Repository : Data Persistent
    - @Transactional : Transaction Management
    ```
    class student extends ServiceBase {

        @Autowired
        StudentDAO studentDAO

        @Transactional(autoCommit=false) //Transaction Management definition
        def findMany(data) {
            def param = [:] //groovy object
            param['email'] = data._param['mail']
            data.resultList = studentDAO.selectActionSchedule([:])
            return data
        }
    }
    ```
* Non-XML : You don't need xml to define SQL, Server Configuration, Class Properties.
* Data Access Object
    - Intuitive SQL Definition
    - Supporting Dynamic Query : using Velocity
    - Define SQL with Groovy Interface : multi-line String available, Refactoring available
    ```
    @Repository() //simalar to Spring Repository.
    interface StudentDAO {
        @Select('''
            SELECT
                A.name,
                A.email
            FROM Student A
            WHERE A.id = :id
              #if($email) /* Dynamic Query with Velocity */
              AND email like concat(:email,'%')
              #end
        ''')
        List selectStudent(Map x)
    ```
* vanillax-batch, running prediodic job. (Cron Expression available, fixed delay batch job available )
* vanillax-webmvc, developing simple web service.

## Sub Projects
* vanillax-core
   - JDBC : JDBC Connection and Java persistent controlling classes
   - Object : Groovy Object Dynamic loading core classes. You can load groovy class without server reboot at all.
* vanillax-webmvc : Web Service Library. Rest based service environment.
* vanillax-batch : vanillax-webmvc based batch system.
   - cron expression available
   - Triggering Fixed delayed Job

## Examples
* Web Service Example : [vanillax-framework-webmvc-example](https://github.com/vanillabrain/vanillax-framework-webmvc-example)
* Batch System Example : [vanillax-framework-batch-example](https://github.com/vanillabrain/vanillax-framework-batch-example)

## 주요특징
* 완전한 POJO 환경
* Java 개발자에 친숙한 개발환경. (Groovy와 Java 코드를 모두 수용함)
  Java Developer friendly development environment. (Groovy and Java code all are available)
* Groovy 객체를 이용한 단순한 데이터 구조. Value Object, DAO Implementation class 등이 모두 불필요함.
* Groovy class들의 완전한 동적로딩. 서버 재구동이 불필요함!!!!!!
* 완전한 DB 접근 자원의 모니터링. Connection 누수 탐지.
* Spring Framework 개잘자에게 친숙한 Service 개발환경. (Spring Framework와 유사한 업무로직 클래스 및 Java Persistent 제어 방식 채용)
    - @Autowired를 이용한 DI구현
    - @Repository를 이용한 Data 접근
    - @Transactional을 이용한 단순한 트랜잭션관리
    ```
    class student extends ServiceBase {

        @Autowired
        StudentDAO studentDAO

        @Transactional(autocommit=false) //Transaction 관리
        def findMany(data) {
            def param = [:] //groovy object
            param['email'] = data._param['mail']
            data.resultList = studentDAO.selectActionSchedule([:])
            return data
        }
    }
    ```
* Non-XML : SQL정의, 서버 Configuration, Class 속성명세 등에 XML을 사용하지 않음.
* Data Access Object 지원
    - 직관적인 SQL 개발환경
    - SQL 작성시 Dynamic Query 지원 : Velocity 문법 사용
    - SQL 개발시 Groovy Interface 를 이용 : 다중 개행 String 지원, Refactoring 가능
    ```
    @Repository() //Spring Repository와 같은 개념. DataSource 이름을 value로 입력
    interface StudentDAO {
        @Select('''
            SELECT
                A.name,
                A.email
            FROM Student A
            WHERE A.id = :id
              #if($email) /* Velocity 문법을 이용한 Dynamic Query */
              AND email like concat(:email,'%')
              #end
        ''')
        List selectStudent(Map x)
    ```
* vanillax-batch를 이용하여 주기적 작업을 수행. (Cron Expression을 이용및 주기적 실행 및 특정시간 간격의 작업 모두 가능 )
* vanillax-webmvc를 이용하여 간편한 웹서비스 개발.

## Sub Project 설명
* vanillax-core
   - JDBC : JDBC Connection 및 자바 persistent 제어를 위한 핵심 클래스
   - Object : Groovy Object를 동적으로 로딩하는 핵심 클래스. 서버 재구동없이 Groovy 클래스를 재로딩하여 완전한 동적 로딩을 구현함.
* vanillax-webmvc : Web Application 개발 환경. Rest 서비스 개발에 최적회 되어 있음.
* vanillax-batch : 위 vanillax-webmvc기반위에 개발된 배치 시스템.
   - cron expression 지원
   - 일정 시간간격별 실행 기능 지원