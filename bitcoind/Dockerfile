FROM armv7/armhf-ubuntu
MAINTAINER Johannes Wenzel

RUN apt-add-repository ppa:bitcoin/bitcoin \
    && apt-get update \
	&& apt-get install bitcoind
	
	
CMD [ "/bin/bash" ]