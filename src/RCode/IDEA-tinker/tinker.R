require(reshape)
require(plyr)
require(dplyr)
#
require(stringr)
#
require(ggplot2)
require(gridExtra)
require(scales)
require(Cairo)
#
#
#
#
dat = read.table("/media/Stock/tmp/asys40_test.out", as.is = T, sep = ",", header = T)
dat$rule_reduction <- dat$prunedRules/dat$grammarRules
head(arrange(dat, rule_reduction))
#
#
#
dat = read.table("/media/Stock/tmp/ecg0606_test.out", as.is = T, sep = ",", header = T)
dat$rule_reduction <- dat$prunedRules/dat$grammarRules
head(arrange(dat[dat$coverage>0.98,], rule_reduction))
plot(hist(dat[dat$coverage>0.98,]$approxDist))
#
#
#
dat = read.table("/media/Stock/tmp/ann_gun_CentroidA1_test.out", as.is = T, sep = ",", header = T)
dat$rule_reduction <- dat$prunedRules/dat$grammarRules
head(arrange(dat[dat$coverage>0.98,], rule_reduction))
plot(hist(dat[dat$coverage>0.98,]$approxDist))

dat = read.table("/media/Stock/tmp/dutch_power_test.out", as.is = T, sep = ",", header = T)
dat$rule_reduction <- dat$prunedRules/dat$grammarRules
head(arrange(dat[dat$coverage>0.98,], rule_reduction))
plot(hist(dat[dat$coverage>0.98,]$approxDist))