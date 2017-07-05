for i in {00201..00250}; do mv blk$i.dat /var/bitcoin/blocks && ln -s /var/bitcoin/blocks/blk$i.dat /bitcoin/blocks/blk$i.dat ; done
