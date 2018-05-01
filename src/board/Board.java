package board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Board {

	public int[] subBoards = new int[9];
	
	private int subBoardsWon;
	
	private int ties;
	
	private boolean isPlayerTwo = false;
	
	private int nextPos = -1;
	
	private boolean isWon;
	
	private int lastRow = -1, lastCol = -1, lastBoard = -1;
	private boolean resetLastBoard = false;
	
	private static final int[] winningCombos = new int[8];
	
	static {
		int i = 0;
		for (int row = 0; row < 3; ++row) {
			winningCombos[i] = 0b111 << (row * 3);
			++i;
		}
		for (int col = 0; col < 3; ++col) {
			winningCombos[i] = 0b001001001 << col;
			++i;
		}
		winningCombos[i] = 0b100010001;
		winningCombos[i + 1] = 0b001010100;
	};

	public Board() {
	}
	
	private void setBoard(int boardNum, int row, int col, boolean isPlayerTwo) {
		int pos = 3 * row + col;
		if (isPlayerTwo) pos += 9;
		subBoards[boardNum] |= (1 << pos);
	}
	
	private int getXBoard(int board) {
		return board & 0777; // first 9 bits
	}
	
	private int getOBoard(int board) {
		return board >> 9; // next 9 bits
	}
	
	private boolean isWinningMove(int board, int row, int col, boolean isPlayerTwo) {
		int consideredBoard = isPlayerTwo ? getOBoard(board) : getXBoard(board);
		int rowTest = winningCombos[row];
		if ((consideredBoard & rowTest) == rowTest) return true;
		int colTest = winningCombos[3 + col];
		if ((consideredBoard & colTest) == colTest) return true;
		// diagonals
		if (row == col && (consideredBoard & winningCombos[6]) == winningCombos[6]) return true;
		if (row + col == 2 && (consideredBoard & winningCombos[7]) == winningCombos[7]) return true;
		return false;
	}
	
	public boolean isPlayer2Turn() {
		return isPlayerTwo;
	}
	
	public void setBoard(int row, int col) {
		if (nextPos == -1) throw new IllegalArgumentException("Need to specify which region to start in");
		lastRow = row;
		lastCol = col;
		if (resetLastBoard) lastBoard = -1;
		setBoard(nextPos, row, col, isPlayerTwo);
		if (isWinningMove(getSubBoard(), row, col, isPlayerTwo)) {
			subBoardsWon |= (1 << (isPlayerTwo ? nextPos + 9 : nextPos));
			if (isWinningMove(subBoardsWon, nextPos / 3, nextPos % 3, isPlayerTwo)) {
				isWon = true;
			}
		} else if ((getXBoard(getSubBoard()) | getOBoard(getSubBoard())) == 0777) { // board is full
			ties |= (1 << nextPos);
		}
		isPlayerTwo = !isPlayerTwo;
		nextPos = 3 * row + col;
		if (isFinished(nextPos)) nextPos = -1; 
		resetLastBoard = true;
	}

	private int getSubBoard() {
		return subBoards[nextPos];
	}
	
	public boolean isFinished(int n) { // returns true if the nth sub-board is finished and no new tokens can be placed there
		return ((subBoardsWon >> n) & 1) == 1 ||
				((subBoardsWon >> (n + 9)) & 1) == 1 ||
				((ties >> n) & 1) == 1;
	}
	
	private boolean isFilled(int board, int row, int col) {
		return ((getOBoard(board) | getXBoard(board)) >> (3*row + col) & 1) == 1;
	}
	
	public boolean isFilled(int row, int col) {
		if (nextPos == -1) throw new IllegalAccessError("Board number must be specified");
		int board = getSubBoard();
		return isFilled(board, row, col);
	}
	
	public void setBoardNum(int boardNum) {
		if (nextPos != -1) throw new IllegalArgumentException("Board number specified when forced");
		lastBoard = boardNum;
		nextPos = boardNum;
		resetLastBoard = false;
	}
	
	public boolean isBoardForced() {
		return nextPos != -1;
	}
	
	public boolean isTied() {
		// every board is tied or won by someone but no one has won
		return !isWon && (ties | getXBoard(subBoardsWon) | getOBoard(subBoardsWon)) == 0777;
	}
	
	public boolean hasWon() {
		return isWon;
	}
	
	@Override
	public String toString() {
		char[][] allBoards = new char[9][9];
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 3; ++col) {
				copyBoardToChar(allBoards, subBoards[3*row + col], row*3, col*3);
			}
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 9; ++i) {
			for (int j = 0; j < 9; ++j) {
				builder.append(allBoards[i][j]);
				builder.append(" ");
				if (j == 2 || j == 5) builder.append("| ");
			}
			if (i == 2 || i == 5) builder.append("\n----------------------");
			builder.append("\n");
		}
		builder.append("\nMega Board viewed as a whole:\n");
		char[][] winBoard = new char[3][3];
		copyBoardToChar(winBoard, subBoardsWon, 0, 0);
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				builder.append(winBoard[i][j]);
				builder.append(' ');
			}
			builder.append('\n');
		}
		builder.append("Next placement: " + (nextPos == -1 ? "anywhere" : "Board " + nextPos));
		builder.append(" by player " + (isPlayerTwo ? 'O' : 'X') + "\n");
		builder.append("Previous placement: row=" + lastRow + " col=" + lastCol + " board=" + lastBoard + "\n");
		return builder.toString();
	}
	
	private void copyBoardToChar(char[][] arr, int board, int offRow, int offCol) {
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 3; ++col) {
				char c = '-';
				int pos = 3 * row + col;
				if (((board >> pos) & 1) == 1) c = 'X';
				if (((board >> (pos + 9)) & 1) == 1) c = 'O';
				arr[row + offRow][col + offCol] = c;
			}
		}
	}
	
	// If the next move is forced, returns in the standard positioning system (row * 3 + col)
	// Otherwise, returns in 9 * boardNum + 3 * row + col
	public List<Integer> getAllLegalMoves() {
		List<Integer> result = new ArrayList<>();
		if (!isBoardForced()) {
			for (int boardNum = 0; boardNum < 9; ++boardNum) {
				if (isFinished(boardNum)) continue;
				int subBoard = subBoards[boardNum];
				for (int row = 0; row < 3; ++row) {
					for (int col = 0; col < 3; ++col) {
						if (!isFilled(subBoard, row, col)) result.add(9 * boardNum + 3 * row + col);
					}
				}
			}
		} else {
			for (int row = 0; row < 3; ++row) {
				for (int col = 0; col < 3; ++col) {
					if (!isFilled(row, col)) result.add(3 * row + col);
				}
			}
		}
		return result;
	}
	
	public Board copyOf() {
		Board newBoard = new Board();
		newBoard.subBoards = Arrays.copyOf(subBoards, subBoards.length);
		newBoard.subBoardsWon = subBoardsWon;
		newBoard.ties = ties;
		newBoard.isPlayerTwo = isPlayerTwo;
		newBoard.nextPos = nextPos;
		newBoard.isWon = isWon;
		newBoard.lastBoard = lastBoard;
		newBoard.lastRow = lastRow;
		newBoard.lastCol = lastCol;
		newBoard.resetLastBoard = resetLastBoard;
		return newBoard;
	}

	/**
	 * @return the lastRow
	 */
	public int getLastRow() {
		return lastRow;
	}

	/**
	 * @return the lastCol
	 */
	public int getLastCol() {
		return lastCol;
	}

	/**
	 * @return the lastBoard
	 */
	public int getLastBoard() {
		return lastBoard;
	}
}
