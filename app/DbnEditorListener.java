#ifdef JDK11


import java.awt.*;
import java.awt.event.*;


class DbnEditorListener extends KeyAdapter implements FocusListener {
    DbnGui gui;
    boolean balancing = false;
    TextArea tc;
    int selectionStart, selectionEnd;
    int position;

    public DbnEditorListener(DbnGui gui) {
	this.gui = gui;
    }

    public void keyPressed(KeyEvent event) {
	// only works with TextArea, because it needs 'insert'
	//TextComponent tc = (TextComponent) event.getSource();
	tc = (TextArea) event.getSource();
	deselect();
	char c = event.getKeyChar();
	
	//System.err.println((int)c);
	switch ((int) c) {
	case ')':
	    position = tc.getCaretPosition() + 1;
	    char contents[] = tc.getText().toCharArray();
	    int counter = 1; // char not in the textfield yet
	    //int index = contents.length-1;
	    int index = tc.getCaretPosition() - 1;
	    boolean error = false;
	    if (index == -1) {  // special case for first char
		counter = 0;
		error = true;
	    }
	    while (counter != 0) {
		if (contents[index] == ')') counter++;
		if (contents[index] == '(') counter--;
		index--;
		if ((index == -1) && (counter != 0)) {
		    error = true;
		    break;
		}
	    }
	    if (error) {
		//System.err.println("mismatched paren");
		Toolkit.getDefaultToolkit().beep();
		tc.select(0, 0);
		tc.setCaretPosition(position);
	    }
	    tc.insert(")", position-1);
	    event.consume();
	    if (!error) {
		selectionStart = index+1;
		selectionEnd = index+2;
		tc.select(selectionStart, selectionEnd);
		balancing = true;
	    }
	    break;

	case  1: tc.selectAll(); break;  // control a for select all
	}
    }

    protected void deselect() {
	if (!balancing || (tc == null)) return;	
	// bounce back, otherwise will write over stuff
	if ((selectionStart == tc.getSelectionStart()) &&
	    (selectionEnd == tc.getSelectionEnd()))
	    tc.setCaretPosition(position);
	balancing = false;
    }

    public void focusGained(FocusEvent event) { }

    public void focusLost(FocusEvent event) {
	deselect();
    }
}


#endif
