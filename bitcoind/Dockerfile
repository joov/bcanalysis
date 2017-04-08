FROM armv7/armhf-ubuntu
MAINTAINER Johannes Wenzel

RUN apt-add-repository ppa:bitcoin/bitcoin \
    && apt-get update \
	&& apt-get install bitcoind
	
EXPOSE 8332	
	
ENTRYPOINT [ "/usr/bin/bitcoind" ]

CMD [ "-daemon" ]