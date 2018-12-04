脚本一（2018.11.20更新）

yum -y install wget

wget -N --no-check-certificate https://raw.githubusercontent.com/ToyoDAdoubi/doubi/master/ssr.sh && chmod +x ssr.sh && bash ssr.sh

备用脚本二（2018.11.21更新）

如果上面的脚本暂时用不了，可以用下面的备用脚本，备用脚本没有单独做图文教程，自己摸索下就会了。备用脚本卸载命令：./shadowsocksR.sh uninstall

yum -y install wget

wget --no-check-certificate https://raw.githubusercontent.com/teddysun/shadowsocks_install/master/shadowsocksR.sh

chmod +x shadowsocksR.sh

./shadowsocksR.sh 2>&1 | tee shadowsocksR.log