module fibonacci_module

  implicit none
  contains

  recursive function fibonacci(n) result (fib)
    integer, intent(in) :: n
    integer             :: fib
    if (n < 2) then
      fib = n
    else
      fib = fibonacci(n - 1) + fibonacci(n - 2)
    endif
  end function fibonacci

end module fibonacci_module
