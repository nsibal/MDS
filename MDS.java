/** Starter code for LP3
 *  @author Achyut Arun Bhandiwad - AAB180004
 *  @author Nirbhay Sibal - NXS180002
 *  @author Vineet Vats - VXV180008
 */

package aab180004;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MDS {
    TreeMap<Long, MDSEntry> tree;
    HashMap<Long, TreeSet<MDSEntry>> table;

    private class MDSEntry{
        long id;
        ArrayList<Long> description;
        Money price;

        public MDSEntry(long id, ArrayList<Long> description,Money price){
            this.id = id;
            this.description = description;
            this.price = price;

        }

        @Override
        public boolean equals(Object other){
            MDSEntry o = (MDSEntry) other;
            if(this.id == o.id)
                return true;
            else
                return false;
        }

    }

    class PriceComparator implements Comparator<MDSEntry>{

        @Override
        public int compare(MDSEntry o1, MDSEntry o2) {
            if(o1.price.compareTo(o2.price) == 0)
                return -1;
            return o1.price.compareTo(o2.price);
        }

        @Override
        public boolean equals(Object other){
            return true;
        }

    }
    class IDComparator implements Comparator<MDSEntry>{

        @Override
        public int compare(MDSEntry o1, MDSEntry o2) {
            return Long.compare(o1.id,o2.id);
        }

    }

    // Constructors
    public MDS() {
        this.tree = new TreeMap<>();
        this.table = new HashMap<>();
    }

    /* Public methods of MDS. Do not change their signatures.
       __________________________________________________________________
       a. Insert(id,price,list): insert a new item whose description is given
       in the list.  If an entry with the same id already exists, then its
       description and price are replaced by the new values, unless list
       is null or empty, in which case, just the price is updated. 
       Returns 1 if the item is new, and 0 otherwise.
    */
    public int insert(long id, Money price, java.util.List<Long> list) {
        ArrayList<Long> localList = new ArrayList<>(list);
        MDSEntry newEntry = new MDSEntry(id,localList,price);
        if(tree.containsKey(id)){
            if(list.size() == 0){
                localList = tree.get(id).description;
            }
            delete(id);
            insert(id,price,localList);
	        return 0;
        }else{
	        tree.put(id,newEntry);
	        for(Long desc : localList){
	            TreeSet<MDSEntry> set = table.get(desc);
	            if(set == null){
	                TreeSet<MDSEntry> newSet = new TreeSet<>(new PriceComparator());
	                newSet.add(newEntry);
	                table.put(desc, newSet);
                }else{
	                set.add(newEntry);
                }
            }
            return 1;
        }
    }

    // b. Find(id): return price of item with given id (or 0, if not found).
    public Money find(long id) {
	    MDSEntry entry = tree.get(id);
        if(entry != null){
            return entry.price;
        }else{
            return new Money(0,0);
        }
    }

    /* 
       c. Delete(id): delete item from storage.  Returns the sum of the
       long ints that are in the description of the item deleted,
       or 0, if such an id did not exist.
    */
    public long delete(long id) {
        MDSEntry entry = tree.remove(id);
        if(entry ==  null){
            return 0;
        }else{
            long sum = 0;
            for(Long desc : entry.description){
                TreeSet<MDSEntry> set = table.get(desc);
                if(set!=null){
                    sum += desc;
                    if (set.size() > 1) {
                        set.remove(entry);
                    } else {
                        table.remove(desc);
                    }
                }
            }
            return sum;
        }
    }


    /* 
       d. FindMinPrice(n): given a long int, find items whose description
       contains that number (exact match with one of the long ints in the
       item's description), and return lowest price of those items.
       Return 0 if there is no such item.
    */
    public Money findMinPrice(long n) {
        TreeSet<MDSEntry> set = table.get(n);
        if(set != null)
            return set.first().price;
	    else
            return new Money("0.0");
    }

    /* 
       e. FindMaxPrice(n): given a long int, find items whose description
       contains that number, and return highest price of those items.
       Return 0 if there is no such item.
    */
    public Money findMaxPrice(long n) {
        TreeSet<MDSEntry> set = table.get(n);
        if(set != null)
            return set.last().price;
        else
            return new Money("0.0");
    }

    /* 
       f. FindPriceRange(n,low,high): given a long int n, find the number
       of items whose description contains n, and in addition,
       their prices fall within the given range, [low, high].
    */
    public int findPriceRange(long n, Money low, Money high) {
        if(low.compareTo(high) >0 )
            return 0;
        TreeSet<MDSEntry> set = (TreeSet<MDSEntry>) table.get(n);
        int count = 0;
        for(MDSEntry entry : set){
            if(entry.price.compareTo(low) >=0 && entry.price.compareTo(high) <=0 ){
                count++;
            }
        }
	    return count;
    }

    /* 
       g. PriceHike(l,h,r): increase the price of every product, whose id is
       in the range [l,h] by r%.  Discard any fractional pennies in the new
       prices of items.  Returns the sum of the net increases of the prices.
    */
    public Money priceHike(long l, long h, double rate) {
	    SortedMap<Long,MDSEntry> map = tree.subMap(l,true,h,true);
	    double netIncrease = 0;
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.DOWN);
	    for(Map.Entry<Long,MDSEntry> entry : map.entrySet()){
            double price = entry.getValue().price.d + 0.01 * entry.getValue().price.c;
	        double increase = price * rate / 100;
	        price += increase;
            String s = df.format(price);
            entry.getValue().price = new Money(s);
            netIncrease+= increase;

        }
        String s = df.format(netIncrease);
        return new Money(s);
    }

    /*
      h. RemoveNames(id, list): Remove elements of list from the description of id.
      It is possible that some of the items in the list are not in the
      id's description.  Return the sum of the numbers that are actually
      deleted from the description of id.  Return 0 if there is no such id.
    */
    public long removeNames(long id, java.util.List<Long> list) {
        MDSEntry entry = tree.get(id);
        long sum = 0;
        for(Long desc : list){
            TreeSet<MDSEntry> set = table.get(desc);
            if(set.remove(entry)){
                sum+=desc;
            }
        }
        entry.description.removeAll(list);
        return sum;
    }
    
    // Do not modify the Money class in a way that breaks LP3Driver.java
    public static class Money implements Comparable<Money> { 
	long d;  int c;
	public Money() { d = 0; c = 0; }
	public Money(long d, int c) { this.d = d; this.c = c; }
	public Money(String s) {
	    String[] part = s.split("\\.");
	    int len = part.length;
	    if(len < 1) { d = 0; c = 0; }
	    else if(part.length == 1) { d = Long.parseLong(s);  c = 0; }
	    else { d = Long.parseLong(part[0]);  c = Integer.parseInt(part[1]); }
	}
	public long dollars() { return d; }
	public int cents() { return c; }
	public int compareTo(Money other) { // Complete this, if needed
        int compare = Long.compare(this.d,other.d);
	    if( compare != 0){
	        return compare;
        } else{
            return Integer.compare(this.c,other.c);
        }
	}
	public String toString() { return d + "." + c; }
    }
    
}
