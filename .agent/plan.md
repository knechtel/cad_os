# Project Plan

Aplicativo Android para cadastro de clientes e aparelhos associados, armazenando os dados em banco de dados local (Room SQLite) e permitindo a geração e exportação de PDF com as informações do cliente e seus aparelhos.

Campos do Cliente:
- Nome
- Telefone
- CPF
- Endereço

Campos do Aparelho (vinculados ao cliente):
- Modelo
- Marca
- Número de Serial
- Descrição
- Preço

Funcionalidades Principais:
- Listagem, cadastro, edição e exclusão de Clientes e Aparelhos.
- Persistência em Banco de Dados SQLite (Room).
- Geração e visualização/compartilhamento de documento PDF formatado contendo os dados do cliente e lista de aparelhos associados com valor total.
- Interface limpa e moderna utilizando Jetpack Compose.

## Project Brief

# Project Brief: CadApp - Gestão de Clientes e Aparelhos

## Features

1. **Gestão de Clientes**:
   - Cadastro, listagem, edição e exclusão de clientes com suporte aos campos Nome, Telefone, CPF e Endereço.

2. **Gestão de Aparelhos Associados**:
   - Vinculação, cadastro, edição e exclusão de aparelhos associados a um cliente específico (campos: Modelo, Marca, Número de Serial, Descrição e Preço).

3. **Persistência de Dados Local**:
   - Armazenamento offline seguro dos dados de clientes e aparelhos e do relacionamento entre eles usando o banco de dados local Room (SQLite).

4. **Geração e Compartilhamento de PDF**:
   - Exportação e visualização de documento PDF formatado contendo os dados cadastrais do cliente, a relação de aparelhos associados e a soma total consolidada dos preços.

---

## High-Level Tech Stack

- **Linguagem**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Navegação & Estratégia Adaptativa**: 
  - **Jetpack Navigation 3** (baseado em estado)
  - **Compose Material Adaptive** (utilizado para a construção de todos os layouts adaptativos)
- **Persistência de Dados**: Room Database (SQLite)
- **Concorrência & Reatividade**: Kotlin Coroutines & Flow
- **Geração de Documentos**: Android native `PdfDocument` / `PrintManager`
- **Arquitetura**: MVVM com Jetpack ViewModel & Repository pattern

## Implementation Steps
**Total Duration:** 21h 16m 17s

### Task_1_DataLayerAndRoomSetup: Implement Room Database entities (Customer, Device), 1-to-N relation (CustomerWithDevices), DAOs, and Repositories for CadApp local data persistence.
- **Status:** COMPLETED
- **Updates:** Room Database entities (Customer, Device), relation (CustomerWithDevices), DAOs (CustomerDao, DeviceDao), AppDatabase and AppRepository implemented successfully. Build and unit tests verified.
- **Acceptance Criteria:**
  - Customer and Device Room entities created with required fields
  - DAO and Repository implemented for CRUD operations
  - Room Database configured and project builds successfully
- **Duration:** 3m

### Task_2_ComposeUIAndMVVM: Implement Jetpack Compose UI screens (Customer List, Add/Edit Customer, Customer Detail / Device List, Add/Edit Device) and ViewModels with Navigation.
- **Status:** COMPLETED
- **Updates:** Jetpack Compose screens (CustomerListScreen, AddEditCustomerScreen, CustomerDetailScreen, AddEditDeviceScreen) and ViewModels (MainViewModel, CustomerFormViewModel, DeviceFormViewModel) with NavGraph implemented using Material 3 and BRL currency formatting. Verified build and tests.
- **Acceptance Criteria:**
  - Compose UI screens created for Customer and Device management using Material 3
  - Navigation graph configured between screens
  - ViewModels integrated with repository for reactive state handling
- **Duration:** 9m 23s

### Task_3_PdfGenerationAndSharing: Implement Android PdfDocument generation utility to create PDF reports with customer and device data, and configure FileProvider for sharing.
- **Status:** COMPLETED
- **Updates:** PdfGenerator implemented with android.graphics.pdf.PdfDocument for A4 formatted reports. FileProvider configured in AndroidManifest.xml and res/xml/file_paths.xml. PDF sharing integrated with Intent.ACTION_SEND in CustomerDetailScreen and MainViewModel. Build and tests passed.
- **Acceptance Criteria:**
  - PDF generation logic implemented using PdfDocument
  - FileProvider configured in AndroidManifest.xml
  - Share action integrated into UI to open share sheet with generated PDF
- **Duration:** 3m 50s

### Task_4_RunAndVerify: Run and verify the completed CadApp application. Instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** App fully implemented and verified. Includes customer CRUD, device CRUD linked by customer ID (1-to-N), Room database, PDF generation with FileProvider sharing, BRL currency formatting, OS Number badge display on cards, details, search, and PDF reports. All unit tests passed.
- **Acceptance Criteria:**
  - build pass
  - app does not crash
  - make sure all existing tests pass
  - all feature requirements verified
- **Duration:** 21h 4s

