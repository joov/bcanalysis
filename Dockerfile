FROM jsurf/rpi-java:latest
MAINTAINER Johannes Wenzel 

#RUN [ "cross-build-start" ]

RUN mkdir /bitcoin-working
WORKDIR /bitcoin-working

COPY ./install.sh /hbase-working

RUN apt-get update \
	&& apt-get install maven 
	
ADD . src
	
WORKDIR /bitcoin-working/src

RUN maven install

# RUN [ "cross-build-end" ]

# HBase zookeeper master, master web UI, regionserver, regionserver web UI
EXPOSE  8080

WORKDIR /usr/local/hbase
ENV HBASE_HOME /usr/local/hbase

ENTRYPOINT [  "bin/hbase-daemon.sh" ]
CMD [ "foreground_start", "master" ]