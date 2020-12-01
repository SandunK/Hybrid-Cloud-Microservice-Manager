# Hybrid Cloud Microservice Manager
This project is to isolate your services that have heavy computational using cloud computing (Here we used AWS) with middle layer load balancing in a more economical way. This is a collection of microservices that contribute to the service discovery, load balancing and AWS instance management using _'Aws SDK for Java'_. 

## Content
This document will consist of initial Setup of the sub components,  
  - Eureka server registry 
  - Load balancer
  - Alpha service on aws instance (Backtest service will be discussed)
  
## Eureka server registry
Eureka server registry will keep track of the microservices that deployed and registered with eureka. Service instances sends heartbeat to the Eureka server registry to inform that they are available. Loadbalancer will take the microservice instance details when needed from Eureka server registry. Executable **server.jar** file will be used to deploy. 
  1. On linux envirenment
        ```
        $ {path to the jar file}/server.jar 
        ```
Eureka server registry will be deployed on port **8761** by default. If you want to change the port use this command.
        ```
        $ {path to the jar file}/server.jar --server.port={port number}
        ```
## Load Balancer
 Load balancer should be configured on a server which available always since service requests directly trigger into the loadbalancer. That can be an os level service or service deployed on a docker container. Executable **loadbalancer.jar** file will be used to deploy.     
  
  1. Configure the envirenment
        - Install "awscli" on server 
            ```
            centOs - sudo yum install awscli
            ubuntu - sudo apt-get install awscli
            ```
            > Note: On testing purposes "aws-cli" version 1.16.266 was used.
            
        - Give credentials of the user that have access to the aws instances that are using.
            There are several ways of giving credential to the aws sdk. You can refer them [here](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html). Preffered method is using aws credential profile file named **credentials** which located in the path ```/home/user/.aws```. Append following content into the credentials file.
            ```
            [loadBalancer]
            aws_access_key_id={YOUR_ACCESS_KEY_ID}
            aws_secret_access_key={YOUR_SECRET_ACCESS_KEY}
            ```
            > "aws_access_key_id" and "aws_secret_access_key" should be get from a user that have access to the aws instances which are using to host the services. [REFERENCES](https://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html#access-keys-and-secret-access-keys)
            
            > Instead of editing "credentials" file you can use aws-cli command ```$ aws configure --profile=loadBalancer``` and give access key and secret using bash.
            
  2. Deployment On linux envirenment
    
        ```
        $ {path to the jar file}/loadbalancer.jar --eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka
        ```
- "eureka.client.serviceUrl.defaultZone" is the access url of Eureka server registry. "localhost" in above command should be replaced with public ip of the server that deployed "Eureka server registry".

- Loadbalancer will be deployed on port **8111** by default. If you want to change the port use this command.
        ```
        #{path to the jar file}/server.jar --eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka --server.port={port number}
        ```
        
## Micro service on AWS instance

It recomended to use docker based deployment for services in aws instances. This document describes the steps of deploying a service called `Backtest` using docker image.
    
1. First install `aws-cli` on instance as before in loadbalancer configuration.
2. Now execute `aws configure` command on bash and provide credential keys of the user that owns the aws repository that used to build the "Backtest" image.
3. After configuring aws credentials execute `$(aws ecr get-login)`. This will logged you in.
4. Now pull the latest build of the "Backtest" image using `docker pull {imageurl}`. 
    > eg: ` docker pull 563747645825.dkr.ecr.us-west-2.amazonaws.com/test:1.0.1`
5. Next use this docker command to create a start script to start the service.
    ```
    docker run -t -e "eureka_instance_ip_address=$(curl -s checkip.amazonaws.com)" -e "eureka_instance_instance_id=$(ip addr | grep 'state UP' -A2 | tail -n1 | awk -F'[/ ]+' '{print $3}'):8080" -e "eureka_client_serviceUrl_defaultZone=http://{{Eureka server registry ip}}:{{port}}/eureka" -p 8080:8080 -p 5008:5008 {{backtest image url}} | rotatelogs -n 5 /home/centos/backtest/docker.log 100M &
    ```
    > You should edit above commad as 
        - `{{Eureka server registry ip}}` - Eureka server registry server public ip eg: `10.1.0.2`
        - `{{port}}` - Eureka server registry service port (default 8761)
        - `{{backtest image url}}` - Url of backtest image eg: `563747645825.dkr.ecr.us-west-2.amazonaws.com/test:1.0.1`
        
6. Change the permissions of start script file using `sudo chmod +x start`
7. Add this cronetab command to execute the start script at the server startup `@reboot sleep 20; /bin/bash /home/centos/start` into the file using `crontab -e`.


## Additional info

> Note: Micro service spring project in this repo is only created for testings. You can have your own service. But application property file configuration should be as it is exept names and ips.        

> Note: You can use Eureka client as it is. Only thing is ip and port numbers should match in loadbalancer and microservices
