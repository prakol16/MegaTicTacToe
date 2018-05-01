package players;

import board.Board;

public class AIPlayer extends Player {

	private MoveTreeNode topNode;
	private int difficulty;
	
	public AIPlayer(int difficulty) {
		topNode = new MoveTreeNode(-1, -1);
		this.difficulty = difficulty;
	}

	@Override
	public void run(Board b) {
		if (!(b.getLastRow() == -1 && b.getLastCol() == -1)) {
			topNode = topNode.findNode(b.getLastRow(), b.getLastCol(), b.getLastBoard());
		}
		for (int i = 0; i < difficulty; ++i) topNode.simulate(b);
		MoveTreeNode best = topNode.getBestChild();
		System.out.println("Best: " + best);
		best.executeMove(b);
		topNode = best;
		topNode.decapitate();
	}

}
