#!/bin/bash
echo "Starting..."

sudo yum install -y docker

sudo service docker start


sudo docker login -u "claudiocohen" -p "dckr_pat_DTr8DRh510ESU9mUQ6ZbOT6fnlw"
sudo docker pull claudiocohen/customer:1.0.0-SNAPSHOT
sudo docker run -d --name customer -p 8080:8080 claudiocohen/customer:1.0.0-SNAPSHOT
