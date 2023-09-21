program FizzBuzz
  integer :: i

  do i = 1, 100
    if (mod(i, 3) == 0) then
      write(*, '(A)', advance='no') 'Fizz'
    end if

    if (mod(i, 5) == 0) then
      write(*, '(A)', advance='no') 'Buzz'
    end if

    if (mod(i, 3) /= 0 .and. mod(i, 5) /= 0) then
      write(*, '(I0)', advance='no') i
    end if

    write(*, *)
  end do
end program FizzBuzz

