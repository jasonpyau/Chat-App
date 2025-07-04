#!/bin/bash

if sudo lsof -t -i:9080 > /dev/null; then 
    echo "Killing process on port 9080."
    sudo kill -9 $(sudo lsof -t -i:9080) &> /dev/null 
fi
echo "Done."