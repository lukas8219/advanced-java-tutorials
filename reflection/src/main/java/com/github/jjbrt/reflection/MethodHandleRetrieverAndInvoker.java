package com.github.jjbrt.reflection;

import static org.burningwave.core.assembler.StaticComponentContainer.Methods;
import static org.burningwave.core.assembler.StaticComponentContainer.Resources;

import java.lang.invoke.MethodHandle;
import java.security.ProtectionDomain;

import org.burningwave.core.classes.MethodCriteria;
import org.burningwave.core.io.FileSystemItem;

public class MethodHandleRetrieverAndInvoker {
    
	
	
    public static void main(String[] args) {
        try {
			execute();
		} catch (Throwable exc) {
			exc.printStackTrace();
		}
    }
	
	
	public static void execute() throws Throwable {
		//Filtering and obtaining a MethodHandle reference
		MethodHandle defineClassMethodHandle = Methods.findOneDirectHandle(
			MethodCriteria.byScanUpTo((cls) ->
		        //We analyze all the classes between Thread.currentThread().getContextClassLoader().getClass() and 
		    	//ClassLoader class and not all of its hierarchy (default behavior)
		        cls.getName().equals(ClassLoader.class.getName())
		    ).name(
		        "defineClass"::equals
		    ).and().parameterTypes(params -> 
		        params.length == 5
		    ).and().parameterTypesAreAssignableFrom(
		        String.class, byte[].class, int.class, int.class, ProtectionDomain.class
		    ).and().returnType((cls) -> 
		        cls.getName().equals(Class.class.getName())
		    ),
		    Thread.currentThread().getContextClassLoader().getClass()
		);

    	//loading the byte code
    	FileSystemItem currentPath = Resources.get(MethodHandleRetrieverAndInvoker.class).getParent();
    	FileSystemItem fieldsHandlerClassFile = currentPath.findFirstInChildren(
    		FileSystemItem.Criteria.forAllFileThat(
    			fileSystemItem -> fileSystemItem.getName().equals("FieldsHandler.class")
    		)
    	);    	
    	byte[] byteCode = fieldsHandlerClassFile.toByteArray();

		//invoking defineClass method
    	Class<?> fieldsHandlerClass = (Class<?>) defineClassMethodHandle.invoke(
		    Thread.currentThread().getContextClassLoader(),
		    "com.github.jjbrt.reflection.FieldsHandler",
		    byteCode,
		    0,
		    byteCode.length,
		    null
		);
    	
		//Obtaining execute MethodHandle reference
		MethodHandle executeMethodHandle = Methods.findOneDirectHandle(
		    MethodCriteria.byScanUpTo((cls) ->
		        //We only analyze the com.github.jjbrt.reflection.FieldsHandler class and not all of its hierarchy (default behavior)
		        cls.getName().equals(fieldsHandlerClass.getName())
		    ).name(
		        "execute"::equals
		    ).and().parameterTypes(params -> 
		        params.length == 0
		    ), 
		    fieldsHandlerClass
		);
		
    	//Calling execute method of com.github.jjbrt.reflection.FieldsHandler class
		executeMethodHandle.invoke();

    }
    
}
