class DbnException extends Exception
{
    int lnum = -1;

    public DbnException(String s)
    {
        super(s);
    }

    public DbnException(String s, int a)
    {
        super(s);
        lnum = a;
    }
}

