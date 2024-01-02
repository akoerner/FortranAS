! Program 6: Function Example
program program6
  integer :: result

  result = addNumbers(3, 4)

  print *, 'Result:', result

  contains

  function addNumbers(x, y)
    integer, intent(in) :: x, y
    addNumbers = x + y
  end function addNumbers
end program program6


