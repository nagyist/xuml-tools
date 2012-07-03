package xuml.tools.model.compiler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.persistence.EntityManagerFactory;
import javax.xml.bind.JAXBElement;

import miuml.jaxb.Class;
import miuml.jaxb.Domains;
import miuml.jaxb.ModeledDomain;
import miuml.jaxb.Subsystem;
import miuml.jaxb.SubsystemElement;
import xuml.tools.model.compiler.runtime.CreationEvent;
import xuml.tools.model.compiler.runtime.Entity;
import xuml.tools.model.compiler.runtime.Signaller;

/**
 * Generates code associated with one modeled domain.
 * 
 * @author dxm
 * 
 */
public class CodeGeneratorJava {

	private final ModeledDomain domain;
	private final String domainPackageName;
	private final String domainSchema;
	private final Domains domains;

	public CodeGeneratorJava(Domains domains, String domainName,
			String domainPackageName, String domainSchema) {
		this.domains = domains;
		this.domain = Util.getModeledDomain(domains, domainName);
		this.domainPackageName = domainPackageName;
		this.domainSchema = domainSchema;
	}

	public void generate(File destination) {

		ModeledDomain md = domain;
		Lookups lookups = new Lookups(domains, md);
		for (Subsystem subsystem : md.getSubsystem()) {
			for (JAXBElement<? extends SubsystemElement> element : subsystem
					.getSubsystemElement()) {
				if (element.getValue() instanceof Class) {
					Class cls = (Class) element.getValue();
					// create classes (impls)
					createImplementation(cls, destination, lookups);
				}
			}

			// create object factory

			// createPersitenceXml(domain.getClazz(), resourcesDirectory);
		}
		createContext(domain, destination, lookups);
		log("finished generation");
	}

	private void createContext(ModeledDomain domain, File destination,
			Lookups lookups) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(bytes);

		TypeRegister types = new TypeRegister();
		out.format("public class Context {\n\n");
		out.format("    private static %s signaller;\n\n",
				types.addType(Signaller.class));
		out.format("    public static %s signaller() {\n",
				types.addType(Signaller.class));
		out.format("        return signaller;\n");
		out.format("    }\n\n");
		out.format("    public static void sendSignalsInQueue() {\n");
		out.format("        signaller.sendSignalsInQueue();\n");
		out.format("    }\n\n");
		out.format("    public static void stop() {\n");
		out.format("        signaller.stop();\n");
		out.format("    }\n\n");
		out.format(
				"    public static <T extends %s<T>> T create(%s<T> cls, %s<T> event) {\n",
				types.addType(Entity.class),
				types.addType(java.lang.Class.class),
				types.addType(CreationEvent.class));
		out.format("        return signaller.create(cls,event);\n");
		out.format("    }\n\n");
		out.format(
				"    public static %s setEntityManagerFactory(%s emf) {\n\n",
				types.addType(Signaller.class),
				types.addType(EntityManagerFactory.class));
		out.format("        signaller = new %s(emf);\n",
				types.addType(Signaller.class), types.addType(Signaller.class));
		for (Subsystem subsystem : domain.getSubsystem()) {
			for (JAXBElement<? extends SubsystemElement> element : subsystem
					.getSubsystemElement()) {
				if (element.getValue() instanceof Class) {
					Class cls = (Class) element.getValue();
					// create classes (impls)
					ClassInfo info = createClassInfo(cls);
					if (info.hasBehaviour())
						out.format("        %s.setSignaller_(signaller);\n",
								types.addType(info.getClassFullName()));
				}
			}
		}
		out.format("        return signaller;\n");
		out.format("    }\n");
		out.format("}");
		out.close();

		String s = "package " + domainPackageName + ";\n\n";
		s += types.getImports(domainPackageName + ".Context") + "\n";
		s += bytes.toString();

		String filename = domainPackageName.replace(".", "/") + "/Context.java";
		try {
			FileOutputStream fos = new FileOutputStream(new File(destination,
					filename));
			fos.write(s.getBytes());
			fos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void log(String message) {
		java.lang.System.out.println(message);
	}

	private void createImplementation(Class cls, File destination,
			Lookups lookups) {
		ClassWriter w = new ClassWriter(createClassInfo(cls));
		String java = w.generate();
		File file = new File(destination, getClassFilename(cls));
		writeToFile(java.getBytes(), file);
	}

	private ClassInfo createClassInfo(Class cls) {
		Lookups lookups = new Lookups(domains, domain);
		return new ClassInfoFromJaxb(cls, domainPackageName, "description",
				domainSchema, lookups);
	}

	private String getClassJavaSimpleName(Class cls) {
		return cls.getName().replace(" ", "").replace("-", "");
	}

	private String getFullClassName(Class cls) {
		return domainPackageName + "." + getClassJavaSimpleName(cls);
	}

	private String getClassFilename(Class cls) {
		String s = getFullClassName(cls);
		return s.replace(".", "/") + ".java";
	}

	// ----------------------------------------
	// Static Utility Methods
	// -----------------------------------------

	private static void writeToFile(byte[] bytes, File file) {
		try {
			file.getParentFile().mkdirs();
			java.lang.System.out.println("writing to " + file);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(bytes);
			fos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}