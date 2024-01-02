! Program 7: Module Example
module myModule
  real :: pi = 3.14159
end module myModule

program program7
  use myModule

  print *, 'Value of pi:', pi
end program program7


