package com.tbc.paas.mql.domain;

import com.tbc.paas.mql.grammar.Node;
import com.tbc.paas.mql.grammar.SqlGrammar;
import com.tbc.paas.mql.grammar.SqlGrammarTreeConstants;
import com.tbc.paas.mql.grammar.Token;

public class SqlNode implements Node {

	protected Node parent;
	protected Node[] children;
	protected int id;
	protected int globalPrarmeterIndex = -1;
	protected Object value;
	protected SqlGrammar parser;

	public SqlNode(int i) {
		id = i;
	}

	public SqlNode(SqlGrammar p, int i) {
		this(i);
		parser = p;
	}

	public void jjtOpen() {
	}

	public void jjtClose() {
	}

	public void jjtSetParent(Node n) {
		parent = n;
	}

	public Node jjtGetParent() {
		return parent;
	}

	public void jjtAddChild(Node n, int i) {
		if (children == null) {
			children = new Node[i + 1];
		} else if (i >= children.length) {
			Node c[] = new Node[i + 1];
			System.arraycopy(children, 0, c, 0, children.length);
			children = c;
		}
		children[i] = n;
	}

	public Node jjtGetChild(int i) {
		return children[i];
	}

	public SqlNode getChild(int i) {

		return (SqlNode) jjtGetChild(i);
	}

	public int getChildrenCount() {
		return jjtGetNumChildren();
	}

	public Object getValue() {
		return this.jjtGetValue();
	}

	public int jjtGetNumChildren() {
		return (children == null) ? 0 : children.length;
	}

	public void jjtSetValue(Object value) {
		this.value = value;
	}

	public Object jjtGetValue() {
		return value;
	}

	public Token jjtGetFirstToken() {
		return null;
	}

	public void jjtSetFirstToken(Token token) {
		// NP
	}

	public Token jjtGetLastToken() {
		return null;
	}

	public void jjtSetLastToken(Token token) {
		// NP
	}

	/*
	 * You can override these two methods in subclasses of SimpleNode to
	 * customize the way the node appears when the tree is dumped. If your
	 * output uses more than one line you should override toString(String),
	 * otherwise overriding toString() is probably all you need to do.
	 */

	public String toString() {
		return SqlGrammarTreeConstants.jjtNodeName[id] + "[" + getValue() + "]";
	}

	public String toString(String prefix) {
		return prefix + toString();
	}

	/*
	 * Override this method if you want to customize how the node dumps out its
	 * children.
	 */

	public void dump(String prefix) {
		System.out.println(toString(prefix));
		if (children != null) {
			for (int i = 0; i < children.length; ++i) {
				SqlNode n = (SqlNode) children[i];
				if (n != null) {
					n.dump(prefix + " ");
				}
			}
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setGlobalPrarmeterIndex(int index) {
		globalPrarmeterIndex = index;
	}

	public int getGlobalPrarmeterIndex(){
		return this.globalPrarmeterIndex;
	}
}
