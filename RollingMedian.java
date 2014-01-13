package Util;


public class RollingMedian {
    private static final int NBITS = 16;
    private int[][] tree;
    private short[] data;
    private int     K;
    private int     i;

    public RollingMedian( int K ) {
        if( K < 2 ) throw new IllegalArgumentException( "Window size " + K + " must be greater than 1" );
        this.K = K;
        tree = new int[NBITS+1][0x01 << NBITS];
        data = new short[K];
        i    = 0;
    }

//updating tree when new value is inserted
    private void justInsert( short x ) {
        for( int j = 0; j <= NBITS; ++j ) {
            ++tree[j][x];
            x /= 2; /* Note: >> carries the sign bit, >>> does not */
        }
    }

    private void erase( short x ) {
        for( int j = 0; j <= NBITS; ++j ) {
            --tree[j][x];
            x /= 2; /* Note: >> carries the sign bit, >>> does not */
        }
    }
// insert new value
    public void insert( short x ) {
        if( i >= K ) erase( data[i%K] );
        data[(i++)%K] = x;
        justInsert( x );
    }
    public double getMedian() {
        if( K % 2 == 0 ) return( 0.5*(getElement( K/2-1 )+getElement( K/2 )) );
        else             return( getElement( K/2 ) );
    }
    public short getElement( int j ) {
        if( i == 0 ) throw new IllegalStateException( "No data" );
        ++j;
        int a = 0;
        int b = NBITS;
        while( b-- > 0 ) {
            a *= 2;
            if( tree[b][a] < j ) j -= tree[b][a++];
        }
        return (short)a;
    }
    public double getIQR() {
        return 0;
    }
    public short getMin() {
        return getElement( 0 );
    }
    public short getMax() {
        return getElement( K-1 );
    }
    public void clear() {
        i = 0;
        for( int j = 0; j <= NBITS; ++j )
            for( int k = 0; k < tree[j].length; ++k ) tree[j][k] = 0;
    }

   /*public static void main( String[] args ) {
        int nEl = 6;
        RollingMedian median = new RollingMedian( nEl );
        for( int i = 0; i < args.length; ++i )
            median.insert( Short.parseShort( args[i] ) );
        for( int i = 0; i < nEl; ++i )
            System.out.println( "Element " + i + " = " + median.getElement( i ) );
    }*/
}
