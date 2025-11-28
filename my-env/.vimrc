" 基本设置
set nocompatible              " 禁用 Vi 兼容模式
filetype plugin indent on     " 启用文件类型检测、插件和缩进
syntax on                     " 开启语法高亮
set encoding=utf-8            " 使用 UTF-8 编码

" 界面设置
set number                    " 显示行号
set relativenumber            " 显示相对行号
set cursorline                " 高亮当前行
set showcmd                   " 显示输入的命令
set wildmenu                  " 命令行自动补全
set showmatch                 " 显示匹配的括号
set laststatus=2              " 总是显示状态栏

" 搜索设置
set ignorecase                " 搜索时忽略大小写
set smartcase                 " 如果有大写字母，则区分大小写
set incsearch                 " 实时搜索
set hlsearch                  " 高亮搜索结果

" 缩进和制表符
set autoindent                " 自动缩进
set smartindent               " 智能缩进
set expandtab                 " 将制表符转换为空格
set tabstop=4                 " 制表符宽度为4个空格
set shiftwidth=4              " 自动缩进宽度为4个空格
set softtabstop=4             " 退格键删除4个空格

" 文件和备份
set nobackup                  " 不创建备份文件
set nowritebackup             " 不创建写入备份
set noswapfile                " 不创建交换文件
set undofile                  " 保留撤销历史
set undodir=~/.vim/undodir    " 撤销文件目录

" 其他实用设置
set backspace=indent,eol,start " 退格键正常工作
set hidden                    " 允许在未保存时切换缓冲区
set mouse=a                    " 启用鼠标支持
set scrolloff=5               " 光标上下保留5行
set sidescrolloff=5           " 光标左右保留5列
set autoread                  " 文件在外部被修改时自动重新读取

" 快捷键映射
let mapleader = ","           " 将leader键设置为逗号

" 快速保存和退出
nnoremap <leader>w :w<CR>
nnoremap <leader>q :q<CR>
nnoremap <leader>x :x<CR>

" 清除搜索高亮
nnoremap <silent> <leader><space> :nohlsearch<CR>

" 在窗口间移动
nnoremap <C-h> <C-w>h
nnoremap <C-j> <C-w>j
nnoremap <C-k> <C-w>k
nnoremap <C-l> <C-w>l

" 标签页操作
nnoremap <leader>tn :tabnew<CR>
nnoremap <leader>tc :tabclose<CR>
nnoremap <leader>th :tabprev<CR>
nnoremap <leader>tl :tabnext<CR>

" 插件管理 (需要安装vim-plug)
" 安装命令: curl -fLo ~/.vim/autoload/plug.vim --create-dirs \
"     https://raw.githubusercontent.com/junegunn/vim-plug/master/plug.vim
call plug#begin()

" 主题插件
Plug 'morhetz/gruvbox'

" 状态栏美化
Plug 'vim-airline/vim-airline'
Plug 'vim-airline/vim-airline-themes'

" 文件树
Plug 'preservim/nerdtree'

" 自动补全
Plug 'neoclide/coc.nvim', {'branch': 'release'}

" 快速注释
Plug 'preservim/nerdcommenter'

" 模糊查找
Plug 'junegunn/fzf', { 'do': { -> fzf#install() } }
Plug 'junegunn/fzf.vim'

call plug#end()

" 主题设置
if filereadable(expand('~/.vim/plugged/gruvbox/colors/gruvbox.vim'))
    colorscheme gruvbox
    set background=dark
else
    colorscheme desert
endif

" NERDTree 设置
nnoremap <leader>n :NERDTreeToggle<CR>
let NERDTreeShowHidden=1

" coc.nvim 设置
nmap <silent> gd <Plug>(coc-definition)
nmap <silent> gy <Plug>(coc-type-definition)
nmap <silent> gi <Plug>(coc-implementation)
nmap <silent> gr <Plug>(coc-references)

" fzf 设置
nnoremap <leader>f :Files<CR>
nnoremap <leader>b :Buffers<CR>

" 自动命令
augroup vimrc
    autocmd!
    " 保存时自动删除尾随空格
    autocmd BufWritePre * %s/\s\+$//e
    " 特定文件类型的设置
    autocmd FileType python setlocal shiftwidth=4 tabstop=4
    autocmd FileType javascript setlocal shiftwidth=2 tabstop=2
    autocmd FileType html setlocal shiftwidth=2 tabstop=2
    autocmd FileType css setlocal shiftwidth=2 tabstop=2
    autocmd FileType markdown setlocal wrap linebreak
augroup END
