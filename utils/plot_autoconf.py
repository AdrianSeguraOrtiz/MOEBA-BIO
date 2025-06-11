from collections import defaultdict
import matplotlib.pyplot as plt
import re

def read_parameter_evolution_file(file_path):
    """
    Esta función lee un archivo de evolución de parámetros y devuelve un diccionario con los valores.
    
    :param file_path: Ruta del archivo a leer.
    :return: Diccionario con los valores leídos.
    """
    # Inicializar el diccionario de resultados
    result_dict = {}
    
    # Abrir el archivo como texto y leer línea por línea
    with open(file_path, 'r') as file:
        for line in file:
            # Limpiar espacios en blanco al principio y final de la línea
            line = line.strip()

            # Dividir la línea en clave y valor
            if ":" in line:
                key, value_str = line.split(": ", 1)
                key = key.strip()
                value_str = value_str.strip()
                
                # Convertir el valor a lista de listas si está en el formato {[...]}
                value = value_str.replace("{", "[").replace("}", "]")
                string_cleaned = value.strip("[]")
                groups = re.split(r'\], \[\s*', string_cleaned)
                result = [group.split(', ') for group in groups]
                
                # Reemplazar los valores "null" por None
                result = [[None if val == 'null' else val for val in group] for group in result]
                
                # Almacenar en el diccionario
                result_dict[key] = result
    
    return result_dict

def get_time_series_from_numeric(data_dict, key):
    """
    Esta función toma un diccionario de datos y una clave, y devuelve una serie temporal.
    
    :param data_dict: Diccionario de datos.
    :param key: Clave a extraer.
    :return: Serie temporal extraída.
    """
    # Obtener vector de valores CE
    values = data_dict[key]

    # Inicialización de las listas para los mínimos, promedios y máximos
    minimums = []
    averages = []
    maximums = []

    # Iterar sobre cada sublista
    for sublista in values:
        sublista_numeros = [float(valor) for valor in sublista if valor is not None]
        
        if len(sublista_numeros) == 0:
            continue

        minimum = min(sublista_numeros)
        average = sum(sublista_numeros) / len(sublista_numeros)
        maximum = max(sublista_numeros)

        if minimums:
            minimum = min(minimum, minimums[-1])
            average = averages[-1] if not averages else sum(sublista_numeros) / len(sublista_numeros)
            maximum = max(maximum, maximums[-1])

        minimums.append(minimum)
        averages.append(average)
        maximums.append(maximum)
    
    return [minimums, averages, maximums]

def get_time_series_from_string(data_dict, key):
    # Obtener el vector de objetivos
    values = data_dict[key]

    # Diccionario para almacenar los resultados
    result = defaultdict(lambda: [0] * len(values))

    # Recorrer cada sublista
    for i, sublist in enumerate(values):
        # Si hay algún None en la sublista se continua
        if None in sublist:
            continue
        # Procesar
        for entry in sublist:
            words = entry.split(';')  # Dividir por ';'
            for word in words:
                result[word][i] += 1  # Aumentar el contador en la posición correcta

    # Convertir a dict (opcional, ya que defaultdict también funciona)
    result = trim_zeros_from_dict(dict(result))
    
    return list(result.keys()), list(result.values())
    

def trim_zeros_from_dict(dictionary):
    # Encontrar la posición máxima donde al menos un valor no es 0
    max_len = 0
    for values in dictionary.values():
        for i in range(len(values) - 1, -1, -1):
            if values[i] != 0:
                max_len = max(max_len, i + 1)
                break
    
    # Recortar todas las listas hasta la longitud encontrada
    for key in dictionary:
        dictionary[key] = dictionary[key][:max_len]
    
    return dictionary

# Función para graficar múltiples series temporales
def plot_time_series(series_list, labels=None, title="Series temporales", xlabel="Tiempo", ylabel="Valor", output_file="plot_autoconf.pdf"):
    """
    Esta función grafica múltiples series temporales en la misma gráfica.
    
    :param series_list: Lista de listas, donde cada sublista es una serie temporal.
    :param labels: Lista de etiquetas para las series (opcional).
    :param title: Título de la gráfica.
    :param xlabel: Etiqueta del eje X.
    :param ylabel: Etiqueta del eje Y.
    """
    
    # Verifica si se proporcionaron etiquetas, de lo contrario genera etiquetas por defecto
    if labels is None:
        labels = [f"Serie {i+1}" for i in range(len(series_list))]

    # Comienza a graficar
    plt.figure(figsize=(10, 6))
    
    # Grafica cada serie temporal
    for i, series in enumerate(series_list):
        plt.plot(series, label=labels[i])
    
    # Añadir leyenda, títulos y etiquetas
    plt.legend()
    plt.title(title, fontsize=16)
    plt.xlabel(xlabel, fontsize=14)
    plt.ylabel(ylabel, fontsize=14)
    
    # Mostrar la gráfica
    plt.savefig(output_file)

# Read supervised file
supervised_dict = read_parameter_evolution_file("MOEBA-Parameterization_supervised/ce-ParameterizationFunVarCleanerObserver.csv")
    
# CE evolution
ce_time_series = get_time_series_from_numeric(supervised_dict, "Objective - 0")
plot_time_series([ce_time_series[0]], ["Min"], "Clustering Error Evolution", "Generation", "Clustering Error", "ce_evolution.pdf")

# Objectives evolution
keys, objectives_time_series = get_time_series_from_string(supervised_dict, "--str-fitness-functions")
plot_time_series(objectives_time_series, keys, "Evolution of the presence of objectives", "Generation", "Number of individuals", "objectives_evolution.pdf")

# Read unsupervised file
unsupervised_dict = read_parameter_evolution_file("MOEBA-Parameterization_unsupervised/hv-ParameterizationFunVarCleanerObserver.csv")

# HV evolution
hv_time_series = get_time_series_from_numeric(unsupervised_dict, "Objective - 0")
plot_time_series([hv_time_series[0]], ["Min"], "-1 * Hypervolume Evolution", "Generation", "-1 * Hypervolume", "hv_evolution.pdf")

# Algorithm evolution
keys, algorithms_time_series = get_time_series_from_string(unsupervised_dict, "--str-algorithm")

plot_time_series(algorithms_time_series, keys, "Evolution of the presence of algorithms", "Generation", "Number of individuals", "algorithms_evolution.pdf")

