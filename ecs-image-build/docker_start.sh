#!/bin/bash
#
# Start script for psc-search-consumer

PORT=18639
exec java -jar -Dserver.port="${PORT}" "psc-search-consumer.jar"