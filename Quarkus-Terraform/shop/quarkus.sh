#!/bin/bash
echo "Starting..."

sudo yum install -y docker

sudo service docker start

sudo docker login -u "claudiocohen" -p "dckr_pat_DTr8DRh510ESU9mUQ6ZbOT6fnlw"
sudo docker pull claudiocohen/shop:1.0.0-SNAPSHOT
sudo docker run -d --name shop -p 8080:8080 claudiocohen/shop:1.0.0-SNAPSHOT
