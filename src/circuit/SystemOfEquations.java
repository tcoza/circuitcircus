package circuit;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Describes a system of linear equations, including a method to find the solutions to it
 * @author root
 * @param <T>
 */
public class SystemOfEquations<T extends Negatable>
{
	/** List of equations in system */
	private final List<T> unknowns;
	private final List<Equation> equations;
	
	/** Initialize new empty system of linear equations */
	public SystemOfEquations()
	{
		unknowns = new ArrayList<>();
		equations = new ArrayList<>();
	}
	
	/**
	 * 
	 * @return number of equations currently added to system
	 */
	public int countEquations() { return equations.size(); }
	
	public boolean unknownExists(T u) { return unknowns.contains(u) || unknowns.contains(u.negate()); }
	public List<T> getUnknowns() { return unknowns; }
	
	public boolean hasCoefficient(int unknown)
	{
		for (Equation eq : equations)
			if (eq.getCoefficient(unknown).doubleValue() != 0)
				return true;
		return false;
	}
	
	public double[] solveSystem(boolean supressWarnings) throws Exception
	{
		double[][] matrix = new double[equations.size()][unknowns.size() + 1];
		for (int i = 0; i < matrix.length; i++)
		{
			for (int j = 0; j < unknowns.size(); j++)
				matrix[i][j] = equations.get(i).getCoefficient(j).doubleValue();
			matrix[i][unknowns.size()] = equations.get(i).getConstant();
		}
		
		if ((matrix.length + 1) != matrix[0].length)
			throw new Exception("Error: Incorrect format for system of equations");
		
		double[] solutions = new double[matrix.length];
		solveSystem(0, solutions, supressWarnings, matrix);
		return solutions;
	}

	/**
	 * Iterates through matrix to get identity
	 * @param row
	 * @param solutions
	 * @throws Exception 
	 */
	public void solveSystem(int row, double[] solutions, boolean supressWarnings, double[][] matrix) throws Exception
	{
		if (row == matrix.length)
			return;
		
		{
			int max = row;
			for (int row2 = row + 1; row2 < matrix.length; row2++)
				if (Math.abs(matrix[row2][row]) > Math.abs(matrix[max][row]))
					max = row2;
			double[] temp = matrix[row];
			matrix[row] = matrix[max];
			matrix[max] = temp;
			
			if (!supressWarnings && matrix[row][row] == 0)
				throw new Exception("Error: can't solve system of equations: missing coefficient for variable " + row);
		}
		
		for (int col = row + 1; col < matrix[row].length; col++)
			if (matrix[row][col] != 0)
				matrix[row][col] /= matrix[row][row];
		//matrix[row][row] = 1;
		
		for (int row2 = row + 1; row2 < matrix.length; row2++)
		{
			if (matrix[row2][row] != 0)
				for (int col = row + 1; col < matrix[row2].length; col++)
					if (matrix[row][col] != 0)
						matrix[row2][col] -= matrix[row][col] * matrix[row2][row];
			//matrix[row2][row] = 0;
		}
		
		solveSystem(row + 1, solutions, supressWarnings, matrix);
		solutions[row] = matrix[row][matrix[row].length - 1];
		
		if (solutions[row] != 0)
			for (int row2 = 0; row2 < row; row2++)
			{
				if (matrix[row2][row] != 0)
					matrix[row2][matrix[row2].length - 1] -= solutions[row] * matrix[row2][row];
				//matrix[row2][row] = 0;
			}
	}
	
	public final StringBuilder log = new StringBuilder();
	private void log(double[][] matrix)
	{
		if (true) return;		// Disable
		
		for (double[] e : matrix)
		{
			for (int i = 0; i < e.length - 1; i++)
				log.append(e[i]).append('\t');
			log.append('|').append('\t').append(e[e.length-1]).append('\n');
		}
		log.append('\n');
	}
	
	/**
	 * Prints a programmer friendly output to stderr to visualize system of equations
	 */
	@Override
	public String toString()
	{
		String out = new String();
		for (Equation e : equations)
		{
			for (int i = 0; i < unknowns.size(); i++)
				out += e.getCoefficient(i) + ",\t";
			out += "|\t" + e.getConstant() + "\n";
		}
		return out;
	}
	
	/**
	 * Describes a linear equation
	 */
	public interface Equation<N extends Number>
	{
		public N getCoefficient(int i);
		public double getConstant();
		public void add();
	}
	
	public class StandardEquation implements Equation<Double>
	{
		/** Contains each coefficient to an unknown, described by an index */
		private final List<Double> coefficients = new ArrayList<>();
		/** The constant in the other side of the equal sign */
		private double constant = 0;
		
		/**
		 * 
		 * @return false if no coefficients are all zero (ie: if equation is useless
		 */
		public boolean isConsistent()
		{
			for (int i = 0; i < coefficients.size(); i++)
				if (coefficients.get(i) != 0) return true;
			return false;
		}
		
		/**
		 * Sets the coefficient for specified index to specified value
		 * @param i
		 * @param c 
		 */
		public void setCoefficient(int i, double c)
		{
			for (int j = coefficients.size(); j <= i; j++)
				coefficients.add(0d);
			coefficients.set(i, c);
		}
		
		public void setCoefficient(T u, double c)
		{
			int index = SystemOfEquations.this.unknowns.indexOf(u);
			if (index == -1)
			{
				index = SystemOfEquations.this.unknowns.indexOf(u.negate());
				c *= -1;
			}
			this.setCoefficient(index, c);
		}
		
		/**
		 * Increments the constant by specified value
		 * @param v 
		 */
		public void incrementConstant(double v) { constant += v; }
		
		/**
		 * 
		 * @param i
		 * @return the coefficient of the specified index
		 */
		@Override
		public Double getCoefficient(int i) { return (i < coefficients.size()) ? coefficients.get(i) : 0d; }
		
		@Override
		public double getConstant() { return constant; }
		
		@Override
		public void add() { SystemOfEquations.this.equations.add(this); }
	}
	
	public class DiscreteEquation implements Equation<Integer>
	{
		private final List<Integer> coefficients = new ArrayList<>();
		private double constant = 0;
		
		public boolean isConsistent()
		{
			for (int i = 0; i < coefficients.size(); i++)
				if (coefficients.get(i) != 0) return true;
			return false;
		}
		
		public boolean exists()
		{
			int[] thisEquation = new int[SystemOfEquations.this.unknowns.size()];
			int mul = 0;
			for (int i = 0; i < thisEquation.length; thisEquation[i] *= mul, i++)
				if ((thisEquation[i] = this.getCoefficient(i)) != 0 && mul == 0)
					mul = thisEquation[i];
			
			for (Equation e : SystemOfEquations.this.equations)
				if (e instanceof SystemOfEquations.DiscreteEquation)
				{
					int[] currentEquation = new int[SystemOfEquations.this.unknowns.size()];
					mul = 0;
					for (int i = 0; i < currentEquation.length; currentEquation[i] *= mul, i++)
						if ((currentEquation[i] = ((SystemOfEquations.DiscreteEquation)e).getCoefficient(i)) != 0 && mul == 0)
							mul = currentEquation[i];
					if (Arrays.equals(thisEquation, currentEquation))
						return true;
				}
			return false;
		}
		
		public void setCoefficient(int i, int c)
		{
			for (int j = coefficients.size(); j <= i; j++)
				coefficients.add(0);
			coefficients.set(i, c);
		}
		
		public void setCoefficient(T u, int c)
		{
			int index = SystemOfEquations.this.unknowns.indexOf(u);
			if (index == -1)
			{
				index = SystemOfEquations.this.unknowns.indexOf(u.negate());
				c *= -1;
			}
			this.setCoefficient(index, c);
		}
		
		public void incrementConstant(double v) { constant += v; }
		
		@Override
		public Integer getCoefficient(int i) { return (i < coefficients.size()) ? coefficients.get(i) : 0; }
		
		@Override
		public double getConstant() { return constant; }
		
		@Override
		public void add() { SystemOfEquations.this.equations.add(this); }
	}
	
	public class CircuitVoltageEquation implements Equation<Double>
	{
		private List<Integer> multipliers = new ArrayList<>();
		
		public CircuitVoltageEquation(List<CircuitSegment> loop)
		{
			for (CircuitSegment cs : loop)
			{
				assert !cs.isCurrentSource();
				
				if (!unknownExists((T)cs))
					getUnknowns().add((T)cs);
				this.setCoefficient(cs);
			}
		}
		
		private void setCoefficient(CircuitSegment segment)
		{
			int multiplier = 1;
			int index = getUnknowns().indexOf(segment);
			if (index == -1)
			{
				index = getUnknowns().indexOf(segment.negate());
				multiplier = -1;
			}
			
			for (int j = multipliers.size(); j <= index; j++)
				multipliers.add(0);
			multipliers.set(index, multiplier);
		}
		
		@Override
		public Double getCoefficient(int i)
		{
			if (i < this.multipliers.size())
				return ((CircuitSegment)getUnknowns().get(i)).resistance * this.multipliers.get(i);
			else
				return 0d;
		}

		@Override
		public double getConstant()
		{
			double voltage = 0;
			for (int i = 0; i < this.multipliers.size(); i++)
				voltage += ((CircuitSegment)getUnknowns().get(i)).voltage * this.multipliers.get(i);
			return voltage;
		}

		@Override
		public void add() { SystemOfEquations.this.equations.add(this); }
	}
	
	public class CircuitCurrentEquation implements Equation<Double>
	{
		private final int sign;
		private final int index;
		
		public CircuitCurrentEquation(CircuitSegment segment)
		{
			assert segment.isCurrentSource();
			
			if (!unknownExists((T)segment))
				getUnknowns().add((T)segment);
			
			int multiplier = 1;
			int i = SystemOfEquations.this.unknowns.indexOf(segment);
			if (i == -1)
			{
				i = SystemOfEquations.this.unknowns.indexOf(segment.negate());
				multiplier = -1;
			}
			
			this.index = i;
			this.sign = multiplier;
		}
		
		public boolean exists()
		{
			for (Equation eq : SystemOfEquations.this.equations)
				if (eq instanceof SystemOfEquations.CircuitCurrentEquation)
					if (((CircuitCurrentEquation)eq).index == this.index)
						return true;
			return false;
		}
		
		@Override
		public Double getCoefficient(int i)
		{
			if (this.index == i)
				return new Double(this.sign);
			else
			{
				Double coefficient = ((CircuitSegment)getUnknowns().get(this.index)).
						currentDependencies.get((CircuitSegment)getUnknowns().get(i));
				if (coefficient == null)
				{
					coefficient = ((CircuitSegment)getUnknowns().get(this.index)).
							currentDependencies.get((CircuitSegment)getUnknowns().get(i).negate());
					if (coefficient == null)
						return new Double(0);
					coefficient *= -1;
				}
				
				return -coefficient * sign;
			}
		}

		@Override
		public double getConstant()
		{
			return ((CircuitSegment)getUnknowns().get(this.index)).current;
		}

		@Override
		public void add() { SystemOfEquations.this.equations.add(this); }
	}
}

interface Negatable
{
	public Negatable negate();
}