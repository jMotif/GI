require(reshape)
require(plyr)
require(dplyr)
#
require(stringr)
#
require(lattice)
require(ggplot2)
require(gridExtra)
require(scales)
require(Cairo)
# The palette with grey:
cbPalette <- c("#999999", "#E69F00", "#56B4E9", "#009E73", "#F0E442", "#0072B2", 
               "#D55E00", "#CC79A7")
# The palette with black:
cbbPalette <- c("#000000", "#E69F00", "#56B4E9", "#009E73", "#F0E442", "#0072B2", 
                "#D55E00", "#CC79A7")
# To use for fills, add
# scale_fill_manual(values=cbPalette)
# To use for line and point colors, add
# scale_colour_manual(values=cbPalette)
#
gps=read.table("../resources/test-data/gps_track.txt")
gps_df=data.frame(y=gps$V1,x=1:length(gps$V1))
p_gps = ggplot(data=gps_df, aes(x=x,y=y)) + geom_line()
p_gps
