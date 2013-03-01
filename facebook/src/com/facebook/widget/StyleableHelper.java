package com.facebook.widget;

import java.lang.reflect.Field;

import android.content.Context;

public class StyleableHelper {
	
	/*********************************************************************************
	*   Returns the resource-IDs for all attributes specified in the
	*   given <declare-styleable>-resource tag as an int array.
	*
	*   @param  context     The current application context.
	*   @param  name        The name of the <declare-styleable>-resource-tag to pick.
	*   @return             All resource-IDs of the child-attributes for the given
	*                       <declare-styleable>-resource or <code>null</code> if
	*                       this tag could not be found or an error occured.
	*********************************************************************************/
	public static final int[] getResourceDeclareStyleableIntArray( Context context, String name )
	{
	    try
	    {
	        //use reflection to access the resource class
	        Field[] fields2 = Class.forName( context.getPackageName() + ".R$styleable" ).getFields();

	        //browse all fields
	        for ( Field f : fields2 )
	        {
	            //pick matching field
	            if ( f.getName().equals( name ) )
	            {
	                //return as int array
	                int[] ret = (int[])f.get( null );
	                return ret;
	            }
	        }
	    }
	    catch ( Throwable t )
	    {
	    }

	    return null;
	}

}
