library(plyr)
library(dplyr)
library(stringr)
#
prefixes <- c("A1Benchmark/",
              "A2Benchmark/",
              "A3Benchmark/",
              "A4Benchmark/"
              )
#
ctr <- 1
for (prefix in prefixes) {
  ll = data.frame(base = list.files(path = paste("/media/Stock/tmp/ydata-labeled-time-series-anomalies-v1_0/",
        prefix, sep = ""), pattern = "*.column$",recursive = F), stringsAsFactors = F)
  # str(ll)
  batch <- ddply(ll, .(base), function(x){ paste(
    "java -cp \"jmotif-gi-0.8.6-SNAPSHOT-jar-with-dependencies.jar\" net.seninp.gi.rulepruner.RulePrunerPrinter",
    " -b \"10 300 10 2 15 1 2 10 1\" -d ", paste(prefix,x,sep = ""), 
    " -o ", paste(prefix,x,".out",sep = "")
  )})
  write.table(batch[,2], paste("batch", ctr, ".sh", sep = ""), col.names = F, row.names = F, quote = F)
  ctr = ctr + 1
}
