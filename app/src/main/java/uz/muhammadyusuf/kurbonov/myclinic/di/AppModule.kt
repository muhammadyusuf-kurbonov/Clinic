package uz.muhammadyusuf.kurbonov.myclinic.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import uz.muhammadyusuf.kurbonov.myclinic.repositories.ContactsRepo
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.ContactsRepository
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.MainViewModel

val appModule = module {
    viewModel { MainViewModel(get()) }
    single<ContactsRepository> { ContactsRepo() }
}