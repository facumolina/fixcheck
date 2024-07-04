FROM ubuntu:24.10

RUN apt-get update

# Install requirements
RUN apt-get install -y openjdk-8-jdk
RUN apt-get install -y openjdk-17-jdk
RUN apt-get install -y git
RUN apt-get install -y python3
RUN apt-get install -y python3-pip
RUN apt-get install -y python3-venv
RUN apt-get install -y subversion
RUN apt-get install -y perl
RUN apt-get install -y wget

# Set Java 17 as default
RUN update-alternatives --set java /usr/lib/jvm/java-17-openjdk-amd64/bin/java
RUN update-alternatives --set javac /usr/lib/jvm/java-17-openjdk-amd64/bin/javac
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Clone and build FixCheck
WORKDIR /home/ubuntu
RUN git clone https://github.com/facumolina/fixcheck
WORKDIR /home/ubuntu/fixcheck
RUN ./gradlew shadowJar
RUN python3 -m venv venv
RUN . venv/bin/activate
RUN pip3 install -r experiments/requirements.txt --break-system-packages
RUN pip3 install -r llms/requirements.txt --break-system-packages

# Clone projects related to experiments
WORKDIR /home/ubuntu
RUN git clone https://github.com/rjust/defects4j
RUN git clone https://github.com/Ultimanecat/DefectRepairing