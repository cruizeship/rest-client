## Getting Started
1. Clone repo
2. Download maven (use homebrew on mac)

## Starting the Backend 
### `mvn spring-boot:run`

Runs the backend at [localhost:8080](http://localhost:8080)

### `python gen_embeddings.py`

Start the python service in another terminal window

## API Specs

Available at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Deploying to AWS
1. Commit to main (triggers a CI/CD pipeline --> check Actions)
2. Service should be available at [ec2-52-8-11-14.us-west-1.compute.amazonaws.com:8080](http://ec2-52-8-11-14.us-west-1.compute.amazonaws.com:8080)
