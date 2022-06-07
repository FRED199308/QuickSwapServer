/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lunar.quickswapserver;

/**
 *
 * @author FRED
 */

/**
 *
 * @author FRED_ADMIN
 */
public class IdGenerator {
    
    public static String keyGen()
    {
        int rand=10000 + (int)(Math.random() * ((1000000 - 100000) + 1));
        String k= String.valueOf(rand);
        String key=k;
        return key;
        
    }
    public IdGenerator()
    {
        
    }
    
    
    
}