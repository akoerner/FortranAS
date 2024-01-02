FC = gfortran
FFLAGS = -Wall -Wextra

PROGRAMS = program1 program2 program3 program4 program5 program6 program7 program8 program9 program10

all: $(PROGRAMS)

%: %.f90
	$(FC) $(FFLAGS) -o $@ $<

clean:
	rm -f $(PROGRAMS)
	rm -f *.mod

