package PhilosophersDinner;

import java.util.concurrent.Semaphore;

public class Philosophers {
	
	private static final int THINKING = 0;
	private static final int HUNGRY = 1;
	private static final int EATING = 2; 
	
	private static final long startTime = System.currentTimeMillis();
	private static long programTime = 10000;
	private static long timeToThink = 800;
	private static long timeToEat = 1000;
	
	private static int quantityOfPhilosophers = 5;
	private static Philosopher[] philosophers;
	private static Semaphore mutex;	
	private static Semaphore[] forks; 
	
	private static int[] stateOfThePhilosophers;
	private static long[] timeThatThought;
	private static long[] timeThatAte;
	
	public static void main(String[] args) {
		System.out.println();
		System.out.println("----- CENA DE FILÓSOFOS -----");
		System.out.println();
		
		philosophers = new Philosopher[quantityOfPhilosophers];
		mutex = new Semaphore(1, true);
		forks = new Semaphore[quantityOfPhilosophers];
		
		stateOfThePhilosophers = new int[quantityOfPhilosophers];
		timeThatThought = new long[quantityOfPhilosophers];
		timeThatAte = new long[quantityOfPhilosophers];
		
		for(int index=0; index<quantityOfPhilosophers; index++) {
			forks[index] = new Semaphore(1, true);
			
			try {
				forks[index].acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			stateOfThePhilosophers[index] = THINKING;
			
			timeThatThought[index] = 0;
			timeThatAte[index] = 0;	
		}
		
		for(int index=0; index<quantityOfPhilosophers; index++) {
			philosophers[index] = new Philosopher(index);
		}
		
		for(int index = 0; index<quantityOfPhilosophers; index++) {
			philosophers[index].start();
		}
		
		for(int index=0; index<quantityOfPhilosophers; index++) {
			
			try {
				philosophers[index].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println();
		System.out.println("----- ESTADÍSTICAS -----");
		System.out.println();
		
		for(int index = 0; index<quantityOfPhilosophers; index++) {
			System.out.println("El filósofo " + index + " pensó " + timeThatThought[index] + " milisegundos.");
			System.out.println("El filósofo " + index + " comió " + timeThatAte[index] + " milisegundos.");	
		}
	}
	
	// Left = Position 0, Right = Position 1.
	private static void neighbors(int position, int[] neighbors) {
		// No es el primero, ni el último.
		if ((position != 0) && (position != quantityOfPhilosophers-1)) {
			neighbors[0] = position - 1;
			neighbors[1] = position + 1;
		} else if(position == 0) {
			// Es el primero.
			neighbors[0] = quantityOfPhilosophers - 1;
			neighbors[1] = 1;
		} else {
			// Es el último.
			neighbors[0] = position - 1;
			neighbors[1] = 0;
		}
	}
	
	private static void test(int position) {
		int[] neighbors = new int[2];
		neighbors(position, neighbors);
				
		if ((stateOfThePhilosophers[position] == HUNGRY) && 
			(stateOfThePhilosophers[neighbors[0]] != EATING) && 
			(stateOfThePhilosophers[neighbors[1]] != EATING)
		) {
			stateOfThePhilosophers[position] = EATING;
			forks[position].release();
		}
	}
	
	static class Philosopher extends Thread {
		
		private int position;
		
		public Philosopher (int position) {
			this.position = position;
		}
		
		public void run() {
			while((System.currentTimeMillis() - startTime) < programTime) {
				System.out.println("El filósofo " + this.position + " comenzó a pensar en " + (System.currentTimeMillis() - startTime) + " milisegundos.");
				think();
				System.out.println("El filósofo " + this.position + " dejó de pensar en " + (System.currentTimeMillis() - startTime) + " milisegundos.");
				
				takeForks();
				
				System.out.println("El filósofo " + this.position + " comenzó a comer en " + (System.currentTimeMillis() - startTime) + " milisegundos.");
				eat();
				System.out.println("El filósofo " + this.position + " dejó de comer en " + (System.currentTimeMillis() - startTime) + " milisegundos.");
				
				putForks();
			}
		}
		
		private void think() {
			try {
				Philosopher.sleep(timeToThink);
				
				timeThatThought[this.position] += timeToThink; 
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		private void eat() {
			try {
				Philosopher.sleep(timeToEat);
				
				timeThatAte[this.position] += timeToEat; 
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		private void takeForks() {
			try {
				mutex.acquire();
				
				stateOfThePhilosophers[position] = HUNGRY;
				
				test(this.position);
				
				mutex.release();
				
				forks[position].acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		private void putForks() {
			try {
				mutex.acquire();
				
				stateOfThePhilosophers[position] = THINKING;
				
				int[] neighbors = new int[2];
				neighbors(this.position, neighbors);
				test(neighbors[0]);
				test(neighbors[1]);
				
				mutex.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
