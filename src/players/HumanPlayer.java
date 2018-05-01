package players;

import java.util.Scanner;

import board.Board;

public class HumanPlayer extends Player {
	private Scanner sc;
	public HumanPlayer() {
		sc = new Scanner(System.in);
	}
	
	@Override
	public void run(Board b) {
		boolean free = !b.isBoardForced();
		if (free) {
			int freeBoard;
			while (true) {
				System.out.println("Choose starting location:");
				freeBoard = sc.nextInt();
				if (b.isFinished(freeBoard)) System.out.println("That location is finished");
				else break;
			}
			b.setBoardNum(freeBoard);
		}
		int row, col;
		while (true) {
			System.out.println("row col:");
			row = sc.nextInt();
			col = sc.nextInt();
			if (b.isFilled(row, col)) System.out.println("That cell is not empty");
			else break;
		}
		b.setBoard(row, col);
		
	}

}
