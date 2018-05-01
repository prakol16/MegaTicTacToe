package players;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import board.Board;

public class MoveTreeNode {
	private int row, col, boardNum;
	private HashSet<MoveTreeNode> children;
	private MoveTreeNode parent = null;
	
	private static final double C = 1.4;
	
	private static Random r = new Random();
	
	
	private boolean areChildrenFilled = false;
	private boolean isTerminal = false;
	private double terminalResult = 0;
	
	
	private double wins = 0;
	private int totalGames = 0;
	
	public MoveTreeNode(int row, int col, int boardNum) {
		this.row = row;
		this.col = col;
		this.boardNum = boardNum;
		children = new HashSet<>();
	}
	
	public MoveTreeNode(int row, int col) {
		this(row, col, -1);
	}
	
	public void executeMove(Board b) {
		if (!b.isBoardForced()) {
			if (boardNum == -1) throw new IllegalStateException("Move node does not choose next board");
			b.setBoardNum(boardNum);
		} else if (boardNum != -1) throw new IllegalStateException("Move node cannot chose next board");
		b.setBoard(row, col);
	}
	
	public void simulate(Board b) {
		Board b2 = b.copyOf();
		MoveTreeNode leaf = this;
		while (leaf.areChildrenFilled && !leaf.isTerminal) {
			leaf = selectNext(leaf);
			leaf.executeMove(b2);
		}
		if (!leaf.isTerminal) {
			MoveTreeNode newChild = leaf.expand(b2);
			newChild.executeMove(b2);
			double result;
			if (b2.hasWon()) {
				newChild.isTerminal = true;
				result = newChild.terminalResult = 1;
			} else if (b2.isTied()) {
				newChild.isTerminal = true;
				result = newChild.terminalResult = 0.5;
			} else {
				result = MoveTreeNode.rollout(b2);
			}
			newChild.backPropogate(result);
		} else {
			leaf.backPropogate(leaf.terminalResult);
		}
		
	}
	
	private MoveTreeNode expand(Board b) {
		List<Integer> moves = b.getAllLegalMoves();
		Collections.shuffle(moves);
		for (int i = 0; i < moves.size(); ++i) {
			int move = moves.get(i);
			MoveTreeNode possChild;
			if (b.isBoardForced()) {
				possChild = new MoveTreeNode(move / 3, move % 3);
			} else {
				int boardNum = move / 9;
				move %= 9;
				possChild = new MoveTreeNode(move / 3, move % 3, boardNum);
			}
			if (!children.contains(possChild)) {
				// add possChild
				children.add(possChild);
				possChild.parent = this;
				if (moves.size() == children.size()) areChildrenFilled = true;
				return possChild;
			}
		}
		throw new IllegalStateException("areChildrenFilled is false but children seem filled");
	}
	
	private MoveTreeNode selectNext(MoveTreeNode leaf) {
		double bestUpper = 0;
		MoveTreeNode nodeBestUpper = null;
		for (MoveTreeNode child : leaf.children) {
			double x = child.getUpperBoundWin();
			if (x >= bestUpper) {
				bestUpper = x;
				nodeBestUpper = child;
			}
		}
		return nodeBestUpper;
	}
	
	private double getUpperBoundWin() {
		// Break ties randomly
		return wins / totalGames + C * Math.sqrt(Math.log(parent.totalGames) / totalGames) + r.nextDouble() * 0.000001;
	}
	
	// returns result from the perspective of the player who just moved before the board, or from "O" at the beginning
	public static double rollout(Board b) {
		double result = 0;
		boolean isPlayer2Init = b.isPlayer2Turn();
		while (true) {
			List<Integer> allMoves = b.getAllLegalMoves();
			int finalMove = allMoves.get(r.nextInt(allMoves.size()));
			if (!b.isBoardForced()) {
				b.setBoardNum(finalMove / 9);
				finalMove %= 9;
			}
			b.setBoard(finalMove / 3, finalMove % 3);
//			System.out.println(b.toString());
			if (b.hasWon()) {
				result = b.isPlayer2Turn() == isPlayer2Init ? 1 : 0;
				break;
			} else if (b.isTied()) {
				result = 0.5;
				break;
			}
		}
		return result;
	}
	
	private void backPropogate(double result) {
		totalGames++;
		wins += result;
		if (parent != null) parent.backPropogate(1 - result);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + boardNum;
		result = prime * result + col;
		result = prime * result + row;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MoveTreeNode other = (MoveTreeNode) obj;
		if (boardNum != other.boardNum)
			return false;
		if (col != other.col)
			return false;
		if (row != other.row)
			return false;
		return true;
	}
	
	public MoveTreeNode findNode(int row, int col, int boardNum) {
		for (MoveTreeNode child : children) {
			if (child.row == row && child.col == col && child.boardNum == boardNum)
				return child;
		}
		System.out.println("Wow, unexpected move!");
		return new MoveTreeNode(row, col, boardNum);
	}
	
	// kills parents
	public void decapitate() {
		parent = null;
	}
	
	public MoveTreeNode getBestChild() {
		MoveTreeNode bestChild = null;
		int mostSims = 0;
		for (MoveTreeNode child : children) {
			if (child.totalGames > mostSims) {
				mostSims = child.totalGames;
				bestChild = child;
			}
		}
		return bestChild;
	}
	
	@Override
	public String toString() {
		return "Move: row=" + row + " col=" + col + " boardNum=" + boardNum + " wins/total=" + wins + "/" + totalGames +
				"=" + String.format("%.4f", wins/totalGames) +
				(parent == null ? "" : " UpperBound=" + String.format("%.4f", getUpperBoundWin()));
		
	}
	
	public void printTreeString(int depth) {
		for (int i = 0; i < depth; ++i) System.out.print("  ");
		System.out.println(toString());
		for (MoveTreeNode child : children) {
			child.printTreeString(depth + 1);
		}
	}
}
