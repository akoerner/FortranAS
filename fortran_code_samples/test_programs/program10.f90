! Program 10: Recursive Function
program program10
  integer :: result

  result = factorial(5)

  print *, 'Factorial of 5:', result

  contains

  recursive function factorial(n) result(res)
    integer, intent(in) :: n
    integer :: res

    if (n == 0) then
      res = 1
    else
      res = n * factorial(n - 1)
    end if
  end function factorial
end program program10

