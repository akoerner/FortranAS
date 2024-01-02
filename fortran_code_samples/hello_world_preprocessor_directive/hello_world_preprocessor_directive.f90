program HelloWorld
  character(len=13) :: output_message
  #if defined A
  output_message = "A" 
  #elif define "B"
  output_message = "B" 
  #else
  output_message = "none" 
  #endif
  write(*,*) output_message
end program HelloWorld

