terraform {
  required_version = ">= 1.0.0, < 2.0.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
    }
  }
}

provider "aws" {
  region      = "us-east-1"
}

variable "nbrokers" {
  description = "Number of brokers"
  type        = number
  default     = 3
}

resource "aws_instance" "exampleKafkaConfiguration" {
  ami                     = "ami-045269a1f5c90a6a0"
  instance_type           = "t2.small"
  count                   = var.nbrokers
  vpc_security_group_ids  = [aws_security_group.instance.id]
  key_name                = "vockey"

  user_data = base64encode(templatefile("creation.sh", {
    broker_id = "${count.index + 1}" 
    totalBrokers = var.nbrokers
  }))

  user_data_replace_on_change = true
  
  tags = {
    Name = "terraform-kafka-instance-${count.index + 1}"
  }
}

output "public_dns_list" {
  value = "${formatlist("%v", aws_instance.exampleKafkaConfiguration.*.public_dns)}"
  
}

resource "aws_security_group" "instance" {
  name = var.security_group_name
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
    ingress {
    from_port   = 2181
    to_port     = 2181
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
    ingress {
    from_port   = 9092
    to_port     = 9092
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
    ingress {
    from_port = 3888
    to_port   = 3888
    protocol  = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
    ingress {
    from_port   = 2888
    to_port     = 2888
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }
}

variable "security_group_name" {
  description = "The name of the security group"
  type        = string
  default     = "terraform-example-instance"
}

