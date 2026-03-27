# psc-search-consumer
A service that consumes messages from the stream-company-psc topic and sends data to the primary-search-api

# Build requirements
In order to build ```psc-search-consumer``` locally you will need the following:
- Java 21
- Maven
- Git

# Environment variables

| Name                                | Description                                                                                          | Mandatory   | Example                   |
|-------------------------------------|------------------------------------------------------------------------------------------------------|-------------|---------------------------|
| ```CHS_API_KEY```                   | The client ID of an API key with internal app privileges                                             | √           | abc123def456ghi789        |
| ------                              | -------------                                                                                        | ----------- | ---------                 |
| ```API_URL ```                      | The URL which the Company Appointments API is hosted on                                              | √           | http://api.chs.local:9092 |
| ------                              | -------------                                                                                        | ----------- | ---------                 |
| ```SERVER_PORT```                   | The server port of this service                                                                      | √           | 8081                      |
| ------                              | -------------                                                                                        | ----------- | ---------                 |
| ```CONCURRENT_LISTENER_INSTANCES``` | The number of listeners run in parallel for psc-search-consumer                                      | √           | 1                         |
| ------                              | -------------                                                                                        | ----------- | ---------                 |
| ```TOPIC```                         | The topic id for psc-search-consumer                                                                 | √           | stream-company-psc        |
| ------                              | -------------                                                                                        | ----------- | ---------                 |
| ```GROUP_ID```                      | The group ID for the resource changed Kafka topics                                                   | √           | psc-search-consumer       |
| ------                              | -------------                                                                                        | ----------- | ---------                 |
| ```MAX_ATTEMPTS```                  | The number of times a resource changed message will be retried before being moved to the error topic | √           | 5                         |
| ------                              | -------------                                                                                        | ----------- | ---------                 |
| ```BACKOFF_DELAY```                 | The delay in milliseconds between message republish attempts.                                        | √           | 1000                      |
| ------                              | -------------                                                                                        | ----------- | ---------                 |
| ```INVALID_MESSAGE_TOPIC```         | The invalid message topic id for psc-search-consumer                                                 | √           | invalid-messages          |
| ------                              | -------------                                                                                        | ----------- | ---------                 |





