package a8vg.sdxess;

/**
 * Created by kotaro on 6/13/2017.
 */



/**
 *
 * @author kotaro
 */
public class IPRange implements Comparable {

    public final static int[][] MASKS = {
            {0,  0,  0,  0  }, //0
            {128,0,  0,  0  }, //1
            {192,0,  0,  0  }, //2
            {224,0,  0,  0  }, //3
            {240,0,  0,  0  }, //4
            {248,0,  0,  0  }, //5
            {252,0,  0,  0  }, //6
            {254,0,  0,  0  }, //7
            {255,0,  0,  0  }, //8 - CLASS A
            {255,128,0,  0  }, //9
            {255,192,0,  0  }, //10
            {255,224,0,  0  }, //11
            {255,240,0,  0  }, //12
            {255,248,0,  0  }, //13
            {255,252,0,  0  }, //14
            {255,254,0,  0  }, //15
            {255,255,0,  0  }, //16 - CLASS B
            {255,255,128,0  }, //17
            {255,255,192,0  }, //18
            {255,255,224,0  }, //19
            {255,255,240,0  }, //20
            {255,255,248,0  }, //21
            {255,255,252,0  }, //22
            {255,255,254,0  }, //23
            {255,255,255,0  }, //24 - CLASS C
            {255,255,255,128}, //25
            {255,255,255,192}, //26
            {255,255,255,224}, //27
            {255,255,255,240}, //28
            {255,255,255,248}, //29
            {255,255,255,252}, //30
            {255,255,255,254}, //31
            {255,255,255,255}, //32
    };

    public static int[] getMask(int netmask){
        if( netmask < 0 || netmask > 32 )
            return null;
        return IPRange.MASKS[netmask];
    }

    public static int getNetMask( int[] mask ){
        if( mask.length != 4 )
            return -1;
        for( int i = 0 ; i <= 32 ; i++ ){
            if( mask[0]==IPRange.MASKS[i][0]&&mask[1]==IPRange.MASKS[i][1]&&
                    mask[2]==IPRange.MASKS[i][2]&&mask[3]==IPRange.MASKS[i][3] )
                return i;
        }
        return -1;
    }

    public static boolean isValidMask(int[] mask){
        if( mask.length != 4 )
            return false;
        return IPRange.getNetMask(mask) != -1;
    }

    public static boolean isValidIP(int[] ip){
        if( ip.length != 4 )
            return false;
        for( int i = 0 ; i < 4 ; i++ ){
            if( ip[i] < 0 || ip[i] > 255 )
                return false;
        }
        return true;
    }

    public static String ipToBin(final String ip){
        int len      = ip.length();
        int addr     = 0;
        int fullAddr = 0;
        char [] out  = new char[32];

        for (int i = 0; i < len; i++) {
            char digit = ip.charAt(i);
            if (digit != '.') {
                addr = addr * 10 + (digit - '0');
            } else {
                fullAddr = (fullAddr << 8) | addr;
                addr = 0;
            }
        }
        fullAddr = (fullAddr << 8) | addr;

        for (int i = 0; i < 32; i++, fullAddr <<= 1)
            out[i] = ((fullAddr & 0x80000000) != 0) ? '1' : '0';
        return new String(out);
    }

    /********** Class Starts **************/
    public boolean isValid = false;
    private int[] IP = {0,0,0,0};
    private int[] mask = {0,0,0,0};

    public IPRange(){
        isValid = true;
    }

    public IPRange(String ip, String netmask){
        String[] parts = ip.split("\\.");
        if( parts.length != 4 ){

        }else{
            for( int i = 0 ; i < 4 ; i++ ){
                IP[i] = Integer.parseInt(parts[i]);
            }
            parts = netmask.split("\\.");
            if( parts.length != 4 ){

            }else{
                for( int i = 0 ; i < 4 ; i++ ){
                    mask[i] = Integer.parseInt(parts[i]);
                }

                this.isValid = IPRange.isValidIP(IP) && IPRange.isValidMask(mask );
            }
        }
    }

    public IPRange(String ip, int netmask){
        String[] parts = ip.split("\\.");
        if( parts.length != 4 ){

        }else{
            for( int i = 0 ; i < 4 ; i++ ){
                IP[i] = Integer.parseInt(parts[i]);
            }

            this.mask = IPRange.getMask(netmask);
            this.isValid = IPRange.isValidIP(this.IP) && IPRange.isValidMask(this.mask );

        }
    }

    public IPRange(String ip, int[] mask){
        String[] parts = ip.split("\\.");
        if( parts.length != 4 ){

        }else{
            for( int i = 0 ; i < 4 ; i++ ){
                IP[i] = Integer.parseInt(parts[i]);
            }

            this.mask = mask;
            this.isValid = IPRange.isValidIP(this.IP) && IPRange.isValidMask(this.mask );
        }
    }

    public IPRange(int[] ip, int[] mask){
        this.IP = ip;
        this.mask = mask;
        this.isValid = IPRange.isValidIP(this.IP) && IPRange.isValidMask(this.mask );
    }

    public String getIP(){
        return this.IP[0] + "." + this.IP[1] + "." + this.IP[2] + "." + this.IP[3];
    }

    public String getMask(){
        return this.mask[0] + "." + this.mask[1] + "." + this.mask[2] + "." + this.mask[3];
    }

    @Override
    public String toString(){
        return this.getIP() + " " + this.getMask();
    }

    public String toString(boolean subnetMask){
        if( subnetMask )
            return this.getIP() + "\\" + IPRange.getNetMask( this.mask );
        return this.toString();
    }

    public int[] getIPArray(){
        return this.IP;
    }

    public int[] getMaskArray(){
        return this.mask;
    }

    public int ipToInt(){
        int s = 255;
        return this.IP[3] + this.IP[2]*s + this.IP[1]*s*s + this.IP[0]*s*s*s ;
    }

    public int getNetMask(){
        return IPRange.getNetMask( this.getMaskArray() );
    }

    public String toBin(){
        char[] bin = IPRange.ipToBin(this.getIP()).toCharArray();
        int netmask = this.getNetMask();
        for( int i = 0 ; i < 32-netmask ; i++ ){
            bin[31-i] = 'X';
        }
        return String.valueOf(bin);
    }

    public boolean contains(IPRange other){
        char[] bin1 = this.toBin().toCharArray();
        char[] bin2 = other.toBin().toCharArray();

        if( this.getNetMask() > other.getNetMask() )
            return false;

        for( int i = 0 ; i < bin1.length ; i++ ){
            if( bin1[i] == 'X'  ){
                return true;
            }else{
                if( bin1[i] == bin2[i] ){
                    // is ok!
                }else{
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int compareTo(Object o) {
        IPRange o2 = (IPRange) o ;
        return this.getIP().compareTo(o2.getIP());
    }
}
