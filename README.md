## ChromeDriver Browser with dynamic proxy IP

## Background
* Multiple Amazon buyer accounts are needed for self order purchasing
* These accounts need different IPs
* Chrome browser can use different proxy in different profiles
* Thus a single computer is able to handle many accounts

## Project Design
* A proxy database with API access is built prior to this project
* This program is able to collect proxy IPs and update to the database
* Once an IP is used by an account, the association is saved in the database, then no other account can use it any more
* When an IP is no longer activated, mark as disabled in database
* The program will find next available IP for an account until no more in database

## Dependencies
* ProxyBrowser is dependent on [MailMan](https://github.com/IBPort/MailMan)

## Development Environment
* JDK 1.8.121~
* Maven 3.2.5~
* Intellij IDEA/eclipse/Netbeans