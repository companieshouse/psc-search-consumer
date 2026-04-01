#!/bin/bash
#
# Start script for psc-search-consumer


PORT=8080
exec java -jar -Dserver.port="${PORT}" "psc-search-consumer.jar"