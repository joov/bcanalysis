FROM armv7/armhf-ubuntu
MAINTAINER Johannes Wenzel

RUN apt-get update \
    && apt-get install -y python-software-properties \
    && apt-get install -y software-properties-common \
    && apt-add-repository ppa:bitcoin/bitcoin \
    && apt-get update \
	&& apt-get install -y bitcoind
	
EXPOSE 8332	
	
ENTRYPOINT [ "/usr/bin/bitcoind" ]

CMD [ "-daemon" ]
