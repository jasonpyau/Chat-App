#!/bin/bash

if sudo lsof -t -i:9080 > /dev/null; then 
    sudo kill -9 $(sudo lsof -t -i:9080) &> /dev/null 
fi