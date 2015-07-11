library(shiny)

# Define UI for application that plots random distributions 
shinyUI(pageWithSidebar(
  
  # Application title
  headerPanel("GrammarViz2 sampling explorer."),
  
  # Sidebar with a slider input for number of observations
  sidebarPanel(
    sliderInput("aSize", 
                "Alphabet Size:", 
                min = 1,
                max = 12, 
                value = 4)
  ),
  
  # Show a plot of the generated distribution
  mainPanel(
    plotOutput("samplesPlot",height = 900)
  )
))